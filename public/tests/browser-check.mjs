// Browser coverage never reaches a real Google sign-in or account-deletion endpoint.
// LUMA_PLAYWRIGHT_MODULE can point at an existing Playwright installation.
import http from "node:http";
import fs from "node:fs/promises";
import path from "node:path";
import os from "node:os";
import vm from "node:vm";
import assert from "node:assert/strict";
import { createRequire } from "node:module";
import { deletionConfiguration, FIREBASE_SDK_VERSION } from "../assets/deletion-core.mjs";
const require = createRequire(import.meta.url);
const { chromium } = require(process.env.LUMA_PLAYWRIGHT_MODULE || "playwright");
const root = path.resolve(new URL("..", import.meta.url).pathname);
const output = process.env.LUMA_PAGES_QA_DIR || path.join(os.tmpdir(), "luma-public-pages-qa");
const configScope = { window: {} };
vm.runInNewContext(await fs.readFile(path.join(root, "site-config.js"), "utf8"), configScope, { filename: "site-config.js", timeout: 1000 });
const publishedConfig = configScope.window.LUMA_SITE_CONFIG;
const publishedReady = Boolean(deletionConfiguration(publishedConfig));
const publishedContact = "nabz0r+gaming@gmail.com";
assert.equal(publishedConfig?.supportEmail, publishedContact, "Published support address must preserve its plus tag");
assert.equal(publishedConfig.publisherName, "nabilux", "Published publisher name is exact");
assert.equal(publishedConfig.preparation, true, "Preparation banner remains enabled during qualification");
assert.equal(publishedReady, true, "Published configuration should make the sign-in UI ready without performing sign-in");
await fs.mkdir(output, { recursive: true });
const mime = { ".html": "text/html; charset=utf-8", ".css": "text/css", ".js": "text/javascript", ".mjs": "text/javascript", ".svg": "image/svg+xml", ".png": "image/png", ".txt": "text/plain" };
const server = http.createServer(async (req, res) => {
  try {
    let target = decodeURIComponent(new URL(req.url, "http://localhost").pathname);
    if (target.endsWith("/")) target += "index.html";
    const file = path.resolve(root, `.${target}`);
    if (!file.startsWith(root + path.sep)) throw new Error("outside-root");
    res.setHeader("Content-Type", mime[path.extname(file)] || "application/octet-stream");
    res.end(await fs.readFile(file));
  } catch { res.statusCode = 404; res.end("Not found"); }
});
await new Promise((resolve, reject) => { server.once("error", reject); server.listen(0, "127.0.0.1", resolve); });
const origin = `http://127.0.0.1:${server.address().port}`;
const syntheticEndpoint = "https://wallet-test.run.app/v1/account";
const syntheticAppModule = `https://www.gstatic.com/firebasejs/${FIREBASE_SDK_VERSION}/firebase-app.js`;
const syntheticAuthModule = `https://www.gstatic.com/firebasejs/${FIREBASE_SDK_VERSION}/firebase-auth.js`;
let browser;
let calls = 0;
let response = { status: 200, json: { deleted: true } };
const findings = [], blockedExternal = [], errors = [], failedLocalResources = [];

// A single deny-by-default handler owns every request in every test context.
// External URLs are either fulfilled entirely by explicit synthetic handlers or
// aborted; neither the published wallet URL nor Google's services can be reached.
async function isolatedContext({ configOverride, synthetic = false } = {}) {
  const context = await browser.newContext({ serviceWorkers: "block" });
  await context.route("**/*", async route => {
    const request = route.request(), url = request.url();
    const parsed = new URL(url);
    if (parsed.origin === origin) {
      if (configOverride && parsed.pathname === "/site-config.js") return route.fulfill({ contentType: "text/javascript", body: `window.LUMA_SITE_CONFIG=${JSON.stringify(configOverride)};` });
      return route.continue();
    }
    if (synthetic && url === syntheticAppModule) return route.fulfill({ contentType: "text/javascript", headers: { "access-control-allow-origin": "*" }, body: 'export const getApps=()=>[];export const initializeApp=()=>({name:"test"});' });
    if (synthetic && url === syntheticAuthModule) return route.fulfill({ contentType: "text/javascript", headers: { "access-control-allow-origin": "*" }, body: 'export const getAuth=()=>({});export const inMemoryPersistence={};export const setPersistence=async()=>{};export class GoogleAuthProvider{setCustomParameters(){}};export const signInWithPopup=async()=>({user:{email:"synthetic@example.com",getIdToken:async()=>"synthetic-test-token"}});export const signOut=async()=>{};' });
    if (synthetic && url === syntheticEndpoint) {
      if (request.method() === "OPTIONS") return route.fulfill({ status: 204, headers: { "access-control-allow-origin": origin, "access-control-allow-methods": "DELETE, OPTIONS", "access-control-allow-headers": "Authorization, Content-Type" } });
      assert.equal(request.method(), "DELETE");
      assert.equal(request.headers().authorization, "Bearer synthetic-test-token");
      calls++;
      return route.fulfill({ status: response.status, contentType: "application/json", headers: { "access-control-allow-origin": origin }, body: JSON.stringify(response.json) });
    }
    blockedExternal.push({ method: request.method(), url });
    await route.abort("blockedbyclient");
  });
  context.on("page", page => page.on("pageerror", error => errors.push(error.message)));
  return context;
}
async function assertPublishedContact(page) {
  const links = page.locator('a[href^="mailto:"]:visible');
  assert.ok(await links.count() > 0, "A visible private contact is available");
  for (const link of await links.all()) {
    const href = await link.getAttribute("href");
    const recipient = href.slice("mailto:".length).split("?")[0];
    assert.equal(decodeURIComponent(recipient), publishedContact, "Mail recipient decodes to the exact published plus-tag address");
    assert.match(recipient, /%2b/i, "The literal plus is percent-encoded rather than interpreted as a space");
    assert.equal(await link.textContent(), publishedContact);
  }
}
try {
  browser = await chromium.launch({ headless: true, ...(process.env.LUMA_CHROMIUM_EXECUTABLE ? { executablePath: process.env.LUMA_CHROMIUM_EXECUTABLE } : {}) });
  const context = await isolatedContext();
  context.on("response", response => {
    if (new URL(response.url()).origin === origin && response.status() >= 400) failedLocalResources.push({ url: response.url(), status: response.status() });
  });
  const page = await context.newPage();
  const defaultExternal = [];
  page.on("request", request => { if (new URL(request.url()).origin !== origin) defaultExternal.push(request.url()); });
  for (const viewport of [{ width: 320, height: 740 }, { width: 390, height: 844 }, { width: 1366, height: 1000 }]) {
    await page.setViewportSize(viewport);
    for (const route of ["/index.html", "/privacy/index.html", "/delete-account/index.html"]) {
      for (const lang of ["fr", "en"]) {
        await page.goto(`${origin}${route}?lang=${lang}`);
        await page.waitForFunction(value => document.documentElement.lang === value, lang);
        const fits = await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth);
        if (!fits) {
          console.log(await page.evaluate(() => [...document.querySelectorAll("body *")].filter(el => { const r = el.getBoundingClientRect(); return r.width && (r.right > innerWidth + 1 || r.left < -1); }).map(el => ({ tag: el.tagName, class: el.className, width: el.getBoundingClientRect().width, left: el.getBoundingClientRect().left, right: el.getBoundingClientRect().right })).slice(0, 20)));
          await page.screenshot({ path: path.join(output, "overflow.png"), fullPage: true });
        }
        assert.equal(fits, true, `Overflow: ${route} ${lang} ${viewport.width}`);
        assert.equal(await page.locator("h1:visible").count(), 1);
        assert.ok(await page.locator('a[href*="privacy/index.html"]:visible').count() > 0);
        await assertPublishedContact(page);
        assert.match(await page.locator("[data-publisher]:visible").textContent(), /nabilux/);
        assert.equal(await page.locator("[data-preparation-notice]:visible").count(), 1, "Preparation notice stays visible");
        assert.match(await page.locator("[data-preparation-notice]").textContent(), lang === "fr" ? /Services en qualification/ : /Services under qualification/);
        if (route === "/index.html") {
          const apkLinks = page.locator('a[href="https://raw.githubusercontent.com/nabz0r/AndroidHit/main/releases/luma-drift-1.5.0-rc1.apk"]:visible');
          assert.equal(await apkLinks.count(), 2, "Both direct download actions target the shipped signed APK");
          assert.match(await page.locator(".stat-strip").innerText(), /1 000/);
          assert.match(await page.locator(".sealed-copy").innerText(), lang === "fr" ? /coups illimités/ : /unlimited moves/);
          assert.equal(await page.locator("[data-screenshot-fr]").count(), 3);
          for (const image of await page.locator("[data-screenshot-fr]").all()) {
            const src = await image.getAttribute("src");
            assert.ok(src.endsWith(`-${lang}.png`), "Screenshots follow the selected language");
            await image.scrollIntoViewIfNeeded();
            await page.waitForFunction(img => img.complete && img.naturalWidth > 0, await image.elementHandle());
            await image.evaluate(img => img.decode());
            assert.equal(await image.evaluate(img => img.naturalWidth > 0 && img.naturalHeight > 0), true);
          }
          await page.evaluate(() => scrollTo({ top: 0, behavior: "instant" }));
        }
        if (route.includes("delete-account")) {
          await page.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes(document.documentElement.lang === "fr" ? "Connecte-toi avec Google" : "Sign in with Google"));
          assert.equal(await page.locator("#google-sign-in").isDisabled(), !publishedReady);
          assert.equal(await page.locator("#delete-account").isVisible(), false);
          assert.equal(await page.locator("#delete-confirmation").isVisible(), false);
          assert.match(await page.locator("#deletion-status").textContent(), lang === "fr" ? /Connecte-toi avec Google/ : /Sign in with Google/);
          // Never click this enabled button with the real published configuration.
        }
        if (viewport.width !== 320) {
          const name = route.includes("privacy") ? "privacy" : route.includes("delete") ? "delete-account" : "home";
          await page.screenshot({ path: path.join(output, `${name}-${lang}-${viewport.width}.png`), fullPage: true });
          if (name === "home") await page.screenshot({ path: path.join(output, `${name}-${lang}-${viewport.width}-hero.png`) });
        }
      }
    }
  }
  assert.deepEqual(defaultExternal, [], "Configured default pages must make no external network request");
  assert.deepEqual(failedLocalResources, [], "Every referenced local image/script/style must load without HTTP errors");
  await page.goto(`${origin}/index.html?lang=en`);
  await page.getByRole("button", { name: "FR", exact: true }).click();
  await page.getByRole("navigation").getByRole("link", { name: "Confidentialité", exact: true }).click();
  await page.waitForFunction(() => document.documentElement.lang === "fr");
  await page.getByRole("button", { name: "EN", exact: true }).click();
  await page.getByRole("navigation").getByRole("link", { name: "My account", exact: true }).click();
  await page.waitForFunction(() => document.documentElement.lang === "en");
  assert.equal(new URL(page.url()).searchParams.get("lang"), "en", "Selected language survives real navigation");
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto(`${origin}/index.html?lang=en`);
  assert.equal(await page.evaluate(() => getComputedStyle(document.documentElement).scrollBehavior), "auto");
  await page.keyboard.press("Tab");
  assert.equal(new URL(await page.locator(":focus").getAttribute("href"), origin).hash, "#main", "Keyboard entry exposes the skip link first");
  await page.keyboard.press("Enter");
  assert.equal(new URL(page.url()).hash, "#main");
  findings.push("Homepage direct signed-APK actions, localized actual screenshots, key-challenge scope, loaded resources, cross-page language selection, keyboard skip link and reduced-motion behavior passed");
  findings.push("18 local FR/EN responsive states (320/390/1366px), exact nabilux contact, visible preparation notice, configured sign-in ready without being clicked, zero horizontal overflow and zero default external requests");
  findings.push("Every visible published mailto decodes exactly to nabz0r+gaming@gmail.com, with the literal plus percent-encoded");
  await context.close();

  const missingContext = await isolatedContext({ configOverride: { ...publishedConfig, walletApiBaseUrl: "" } });
  const missingPage = await missingContext.newPage();
  for (const lang of ["fr", "en"]) {
    await missingPage.goto(`${origin}/delete-account/index.html?lang=${lang}`);
    await missingPage.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes(document.documentElement.lang === "fr" ? "Aucune demande" : "sends no request"));
    assert.equal(await missingPage.locator("#google-sign-in").isDisabled(), true);
    assert.equal(await missingPage.locator("#delete-account").isVisible(), false);
    assert.match(await missingPage.locator("#deletion-status").textContent(), lang === "fr" ? /Aucune demande/ : /sends no request/);
    await assertPublishedContact(missingPage);
  }
  assert.equal(calls, 0);
  findings.push("Isolated missing-wallet configuration keeps sign-in disabled in FR/EN and sends no deletion request");
  await missingContext.close();

  // Only this separate context can execute sign-in/deletion, using fully synthetic modules and requests.
  const syntheticConfig = { preparation: true, publisherName: "Test Publisher", supportEmail: publishedContact, walletApiBaseUrl: "https://wallet-test.run.app", firebase: { apiKey: "synthetic", authDomain: "luma-drift-production.firebaseapp.com", projectId: "luma-drift-production", appId: "1:123:web:synthetic" } };
  const syntheticContext = await isolatedContext({ configOverride: syntheticConfig, synthetic: true });
  const syntheticPage = await syntheticContext.newPage();
  await syntheticPage.goto(`${origin}/delete-account/index.html?lang=en`);
  await syntheticPage.locator("#google-sign-in").click();
  await syntheticPage.locator("#delete-confirmation").waitFor({ state: "visible" });
  assert.equal(await syntheticPage.locator("#delete-account").isDisabled(), true);
  assert.equal(calls, 0);
  await syntheticPage.locator("#confirm-delete").check();
  await syntheticPage.locator("#delete-account").click();
  await syntheticPage.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("have been deleted"));
  assert.equal(calls, 1);
  assert.equal(await syntheticPage.locator("#delete-confirmation").isVisible(), false);
  findings.push("Synthetic web sign-in + explicit checkbox + authenticated DELETE + server-confirmed success");
  response = { status: 401, json: { error: { code: "RECENT_LOGIN_REQUIRED" } } };
  await syntheticPage.reload();
  await syntheticPage.locator("#google-sign-in").click();
  await syntheticPage.locator("#confirm-delete").check();
  await syntheticPage.locator("#delete-account").click();
  await syntheticPage.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("Sign in again"));
  assert.equal(await syntheticPage.locator("#delete-confirmation").isVisible(), false);
  findings.push("Synthetic expired-session rejection clears confirmation and requires reauthentication; no success claim");
  response = { status: 200, json: { deleted: false } };
  await syntheticPage.locator("#google-sign-in").click();
  await syntheticPage.locator("#confirm-delete").check();
  await syntheticPage.locator("#delete-account").click();
  await syntheticPage.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("could not be confirmed"));
  findings.push("Synthetic invalid-success response does not announce deletion");
  await syntheticPage.locator("#cancel-delete").click();
  await syntheticPage.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("Sign in with Google"));
  const countBeforeCancel = calls;
  await syntheticPage.locator("#google-sign-in").click();
  await syntheticPage.locator("#cancel-delete").click();
  assert.equal(calls, countBeforeCancel);
  findings.push("Cancel signs out without sending a deletion request");
  await syntheticPage.getByRole("button", { name: "FR", exact: true }).click();
  assert.match(await syntheticPage.locator("[data-contact]").first().textContent(), /demande privée/);
  assert.equal(await syntheticPage.locator('[data-contact] a[href^="mailto:"]').count(), 1);
  await assertPublishedContact(syntheticPage);
  assert.deepEqual(blockedExternal, [], "Unexpected external requests are blocked and fail this test");
  assert.deepEqual(errors, [], "Pages must not throw browser errors");
  assert.equal(calls, 3, "Only the three explicit synthetic DELETE scenarios send deletion requests");
  findings.push("All external requests are denied by default; only in-memory synthetic Firebase modules and synthetic-token DELETE requests are fulfilled");
  findings.push("Configured private email and FR/EN switching work without a public issue request route");
  console.log(JSON.stringify({ status: "passed", findings, screenshots: output, realGoogleAuthenticationTested: false, liveServerDeletionTested: false }, null, 2));
} finally {
  if (browser) await browser.close();
  await new Promise(resolve => server.close(resolve));
}
