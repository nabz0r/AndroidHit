import { deletionConfiguration, deleteVerifiedAccount, FIREBASE_SDK_VERSION } from "./deletion-core.mjs";

const signIn = document.getElementById("google-sign-in");
const confirmPanel = document.getElementById("delete-confirmation");
const confirmCheckbox = document.getElementById("confirm-delete");
const remove = document.getElementById("delete-account");
const cancel = document.getElementById("cancel-delete");
const status = document.getElementById("deletion-status");
const accountLabel = document.getElementById("account-label");
const configuration = deletionConfiguration(window.LUMA_SITE_CONFIG);
let authSdk;
let auth;
let user;
let busy = false;
let state = configuration ? "ready" : "unconfigured";
const strings = {
  fr: {
    ready: "Connecte-toi avec Google pour vérifier le compte Luma Drift à supprimer.",
    unconfigured: "Le service de suppression web attend sa configuration de production. Aucune demande n’est envoyée depuis cette version.",
    signing: "Ouverture de la connexion Google…",
    signed: "Identité vérifiée. Lis la confirmation avant de demander la suppression.",
    deleting: "Suppression en cours. Attends la confirmation du serveur.",
    deleted: "Ton compte Luma Drift et son portefeuille serveur ont été supprimés. Ton compte Google reste intact. Consulte ci-dessous les données d’audit conservées.",
    cancelled: "Connexion annulée. Aucune suppression n’a été demandée.",
    "sign-in-again": "Reconnecte-toi : une authentification récente est nécessaire. Si une suppression précédente est en cours, le serveur la reprendra.",
    "try-later": "Trop de demandes. Patiente avant de réessayer.",
    error: "Impossible de confirmer la suppression. Aucun succès n’est annoncé. Réessaie ou utilise le contact privé ci-dessous ; une demande déjà reçue peut encore être en cours.",
    account: "Compte vérifié : ", anonymous: "compte Google sélectionné"
  },
  en: {
    ready: "Sign in with Google to verify the Luma Drift account you want to delete.",
    unconfigured: "Web deletion is waiting for production configuration. This version sends no request.",
    signing: "Opening Google sign-in…",
    signed: "Identity verified. Read the confirmation before requesting deletion.",
    deleting: "Deletion in progress. Wait for the server confirmation.",
    deleted: "Your Luma Drift account and server wallet have been deleted. Your Google Account stays intact. See the retained audit information below.",
    cancelled: "Sign-in cancelled. No deletion was requested.",
    "sign-in-again": "Sign in again: recent authentication is required. If a previous deletion is in progress, the server will resume it.",
    "try-later": "Too many requests. Wait before trying again.",
    error: "Deletion could not be confirmed. No success is claimed. Retry or use the private contact below; a request already received may still be in progress.",
    account: "Verified account: ", anonymous: "selected Google account"
  }
};
const language = () => document.documentElement.lang === "en" ? "en" : "fr";
function render() {
  const text = strings[language()];
  status.textContent = text[state] || text.error;
  status.className = "status" + (state === "deleted" ? " success" : ["error", "sign-in-again", "try-later"].includes(state) ? " error" : "");
  signIn.disabled = !configuration || busy || !!user || state === "deleted";
  remove.disabled = !user || busy || !confirmCheckbox.checked;
  cancel.disabled = busy;
  confirmPanel.hidden = !user;
  accountLabel.textContent = user ? text.account + (user.email || user.displayName || text.anonymous) : "";
}
async function clearSession() {
  user = undefined;
  confirmCheckbox.checked = false;
  if (authSdk && auth) await authSdk.signOut(auth);
}
signIn.addEventListener("click", async () => {
  if (!configuration || busy) return;
  busy = true; state = "signing"; render();
  try {
    // These Google libraries are fetched only after the player explicitly presses Sign in.
    const [appSdk, loadedAuth] = await Promise.all([
      import(`https://www.gstatic.com/firebasejs/${FIREBASE_SDK_VERSION}/firebase-app.js`),
      import(`https://www.gstatic.com/firebasejs/${FIREBASE_SDK_VERSION}/firebase-auth.js`)
    ]);
    authSdk = loadedAuth;
    const app = appSdk.getApps().find(app => app.name === "luma-account-deletion")
      || appSdk.initializeApp(configuration.firebase, "luma-account-deletion");
    auth = authSdk.getAuth(app);
    auth.languageCode = language();
    await authSdk.setPersistence(auth, authSdk.inMemoryPersistence);
    const provider = new authSdk.GoogleAuthProvider();
    provider.setCustomParameters({ prompt: "select_account" });
    user = (await authSdk.signInWithPopup(auth, provider)).user;
    state = "signed";
  } catch (error) {
    user = undefined;
    state = ["auth/popup-closed-by-user", "auth/cancelled-popup-request"].includes(error?.code) ? "cancelled" : "error";
  } finally { busy = false; render(); }
});
confirmCheckbox.addEventListener("change", render);
cancel.addEventListener("click", async () => {
  if (busy) return;
  busy = true;
  try { await clearSession(); state = "ready"; }
  catch { user = undefined; state = "error"; }
  finally { busy = false; render(); }
});
remove.addEventListener("click", async () => {
  if (!configuration || !user || !confirmCheckbox.checked || busy) return;
  busy = true; state = "deleting"; render();
  try {
    await deleteVerifiedAccount({ endpoint: configuration.endpoint, user });
    // A sign-out failure cannot turn a confirmed server deletion into a reported failure.
    try { await clearSession(); } catch { user = undefined; }
    state = "deleted";
  } catch (error) {
    state = ["sign-in-again", "try-later"].includes(error?.message) ? error.message : "error";
    if (state === "sign-in-again") {
      try { await clearSession(); } catch { user = undefined; }
    }
  } finally { busy = false; render(); }
});
window.addEventListener("luma-language", render);
render();
