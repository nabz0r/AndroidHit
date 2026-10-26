// Pure request/configuration helpers; no network or identity side effects on import.
export const FIREBASE_SDK_VERSION = "13.1.0";
export function deletionConfiguration(config) {
  const firebase = config?.firebase;
  if (!firebase || ![firebase.apiKey, firebase.authDomain, firebase.projectId, firebase.appId]
    .every(value => typeof value === "string" && value.trim())) return null;
  if (!firebase.appId.includes(":web:") || !/^[a-z0-9-]+\.firebaseapp\.com$/i.test(firebase.authDomain)
    || !/^[a-z0-9-]+$/i.test(firebase.projectId)
    || firebase.authDomain !== `${firebase.projectId}.firebaseapp.com`) return null;
  try {
    const base = new URL(config.walletApiBaseUrl);
    const local = ["localhost", "127.0.0.1", "[::1]"].includes(base.hostname);
    if ((base.protocol !== "https:" && !(base.protocol === "http:" && local))
      || base.username || base.password || base.search || base.hash) return null;
    const pathname = base.pathname.replace(/\/+$/, "");
    return { firebase, endpoint: `${base.origin}${pathname}/v1/account` };
  } catch { return null; }
}
export async function deleteVerifiedAccount({ endpoint, user, fetchImpl = fetch }) {
  if (!user || typeof user.getIdToken !== "function") throw new Error("sign-in-required");
  const token = await user.getIdToken(true);
  if (typeof token !== "string" || token.length === 0) throw new Error("sign-in-required");
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 20000);
  try {
    const response = await fetchImpl(endpoint, {
      method: "DELETE",
      headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
      credentials: "omit",
      cache: "no-store",
      redirect: "error",
      referrerPolicy: "no-referrer",
      signal: controller.signal
    });
    let result;
    try { result = await response.json(); } catch { throw new Error("invalid-response"); }
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) throw new Error("sign-in-again");
      if (response.status === 429) throw new Error("try-later");
      throw new Error("server-unavailable");
    }
    if (result?.deleted !== true) throw new Error("invalid-response");
    return true;
  } finally { clearTimeout(timer); }
}
