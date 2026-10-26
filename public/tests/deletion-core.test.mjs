import { test } from "node:test";
import assert from "node:assert/strict";
import { deletionConfiguration, deleteVerifiedAccount } from "../assets/deletion-core.mjs";
const config = {
  walletApiBaseUrl: "https://wallet.example.com/",
  firebase: { apiKey: "public-api-key", authDomain: "luma-drift-production.firebaseapp.com", projectId: "luma-drift-production", appId: "1:123:web:abcdef" }
};
for (const value of [undefined, {}, { ...config, firebase: {} }, { ...config, walletApiBaseUrl: "" }]) {
  test(`incomplete config cannot activate network: ${JSON.stringify(value)}`, () => {
    assert.equal(deletionConfiguration(value), null);
  });
}
test("valid configuration maps to the account endpoint", () => {
  assert.equal(deletionConfiguration(config).endpoint, "https://wallet.example.com/v1/account");
});
for (const value of ["http://wallet.example.com", "javascript:alert(1)", "https://user:password@wallet.example.com", "https://wallet.example.com?token=secret", "https://wallet.example.com#fragment"]) {
  test(`unsafe API address rejected: ${value}`, () => {
    assert.equal(deletionConfiguration({ ...config, walletApiBaseUrl: value }), null);
  });
}
test("loopback HTTP is allowed only for a local development server", () => {
  assert.equal(deletionConfiguration({ ...config, walletApiBaseUrl: "http://127.0.0.1:8080" }).endpoint, "http://127.0.0.1:8080/v1/account");
});
test("Android app configuration cannot activate web auth", () => {
  assert.equal(deletionConfiguration({ ...config, firebase: { ...config.firebase, appId: "1:123:android:abcdef" } }), null);
});
test("unrelated auth domains are rejected", () => {
  assert.equal(deletionConfiguration({ ...config, firebase: { ...config.firebase, authDomain: "attacker.example.com" } }), null);
});
test("auth domain must match the project", () => {
  assert.equal(deletionConfiguration({ ...config, firebase: { ...config.firebase, authDomain: "other.firebaseapp.com" } }), null);
});
test("a user and a fresh token are required before any request", async () => {
  let calls = 0;
  const fetchImpl = async () => { calls++; };
  await assert.rejects(deleteVerifiedAccount({ endpoint: "https://wallet.example.com/v1/account", user: undefined, fetchImpl }), /sign-in-required/);
  await assert.rejects(deleteVerifiedAccount({ endpoint: "https://wallet.example.com/v1/account", user: { getIdToken: async () => "" }, fetchImpl }), /sign-in-required/);
  assert.equal(calls, 0);
});
test("authenticated deletion sends no tokens in URL/body/cookies and rejects redirects", async () => {
  let refreshed = false;
  const result = await deleteVerifiedAccount({
    endpoint: "https://wallet.example.com/v1/account",
    user: { getIdToken: async force => { refreshed = force; return "fresh-test-token"; } },
    fetchImpl: async (url, options) => {
      assert.equal(url, "https://wallet.example.com/v1/account");
      assert.equal(options.method, "DELETE");
      assert.equal(options.headers.Authorization, "Bearer fresh-test-token");
      assert.equal(options.credentials, "omit");
      assert.equal(options.cache, "no-store");
      assert.equal(options.redirect, "error");
      assert.equal(options.referrerPolicy, "no-referrer");
      assert.equal(options.body, undefined);
      assert.ok(options.signal instanceof AbortSignal);
      return { ok: true, json: async () => ({ deleted: true }) };
    }
  });
  assert.equal(refreshed, true);
  assert.equal(result, true);
});
for (const [status, error] of [[401,"sign-in-again"],[403,"sign-in-again"],[429,"try-later"],[503,"server-unavailable"]]) {
  test(`HTTP ${status} never reports deletion success`, async () => {
    await assert.rejects(deleteVerifiedAccount({
      endpoint: "https://wallet.example.com/v1/account",
      user: { getIdToken: async () => "test-token" },
      fetchImpl: async () => ({ ok: false, status, json: async () => ({ deleted: false }) })
    }), new RegExp(error));
  });
}
for (const body of [{}, { deleted: false }, { deleted: "true" }]) {
  test(`ambiguous success body rejected: ${JSON.stringify(body)}`, async () => {
    await assert.rejects(deleteVerifiedAccount({ endpoint: "https://wallet.example.com/v1/account", user: { getIdToken: async () => "test-token" }, fetchImpl: async () => ({ ok: true, json: async () => body }) }), /invalid-response/);
  });
}
test("non-JSON response cannot announce success", async () => {
  await assert.rejects(deleteVerifiedAccount({ endpoint: "https://wallet.example.com/v1/account", user: { getIdToken: async () => "test-token" }, fetchImpl: async () => ({ ok: true, json: async () => { throw new Error("invalid-json"); } }) }), /invalid-response/);
});
