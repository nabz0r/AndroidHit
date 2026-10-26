// Browser coverage uses synthetic Firebase modules, never a real Google account.
// LUMA_PLAYWRIGHT_MODULE can point at an existing Playwright installation.
import http from "node:http";
import fs from "node:fs/promises";
import path from "node:path";
import os from "node:os";
import assert from "node:assert/strict";
import { createRequire } from "node:module";
const require = createRequire(import.meta.url);
const { chromium } = require(process.env.LUMA_PLAYWRIGHT_MODULE || "playwright");
const root = path.resolve(new URL("..", import.meta.url).pathname);
const output = process.env.LUMA_PAGES_QA_DIR || path.join(os.tmpdir(), "luma-public-pages-qa");
await fs.mkdir(output, { recursive: true });
const mime = { ".html": "text/html; charset=utf-8", ".css": "text/css", ".js": "text/javascript", ".mjs": "text/javascript", ".svg": "image/svg+xml", ".txt": "text/plain" };
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
let browser;
const findings = [];
try {
  browser = await chromium.launch({ headless: true, ...(process.env.LUMA_CHROMIUM_EXECUTABLE ? { executablePath: process.env.LUMA_CHROMIUM_EXECUTABLE } : {}) });
  const context = await browser.newContext();
  const page = await context.newPage();
  const external = [];
  const errors = [];
  page.on("request", req => { if (!req.url().startsWith(origin)) external.push(req.url()); });
  page.on("pageerror", error => errors.push(error.message));
  for (const viewport of [{width:320,height:740},{width:390,height:844},{width:1366,height:1000}]) {
    await page.setViewportSize(viewport);
    for (const route of ["/index.html", "/privacy/index.html", "/delete-account/index.html"]) {
      for (const lang of ["fr", "en"]) {
        await page.goto(`${origin}${route}?lang=${lang}`);
        await page.waitForFunction(value => document.documentElement.lang === value, lang);
        const fits = await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth);
        if (!fits) {
          console.log(await page.evaluate(() => [...document.querySelectorAll("body *")].filter(el => {const r=el.getBoundingClientRect();return r.width && (r.right>innerWidth+1 || r.left < -1);}).map(el=>({tag:el.tagName,class:el.className,width:el.getBoundingClientRect().width,left:el.getBoundingClientRect().left,right:el.getBoundingClientRect().right})).slice(0,20)));
          await page.screenshot({path:path.join(output,"overflow.png"),fullPage:true});
        }
        assert.equal(fits, true, `Overflow: ${route} ${lang} ${viewport.width}`);
        assert.equal(await page.locator("h1:visible").count(), 1);
        assert.ok(await page.locator('a[href*="privacy/index.html"]:visible').count() > 0);
        if (route.includes("delete-account")) {
          assert.equal(await page.locator("#google-sign-in").isDisabled(), true);
          assert.equal(await page.locator("#delete-account").isVisible(), false);
          assert.match(await page.locator("#deletion-status").textContent(), lang === "fr" ? /Aucune demande/ : /sends no request/);
        }
        if (viewport.width !== 320 && lang === "fr") {
          const name = route.includes("privacy") ? "privacy" : route.includes("delete") ? "delete-account" : "home";
          await page.screenshot({ path: path.join(output, `${name}-${viewport.width}.png`), fullPage: true });
        }
      }
    }
  }
  assert.deepEqual(external, [], "Default pages must make no external network request");
  assert.deepEqual(errors, [], "Pages must not throw browser errors");
  findings.push("18 FR/EN responsive page states (320/390/1366px), zero horizontal overflow, zero default external requests, zero browser exceptions");
  // Configure only this isolated browser context with synthetic public config.
  await context.route("**/site-config.js", route => route.fulfill({ contentType:"text/javascript", body:'window.LUMA_SITE_CONFIG={preparation:true,publisherName:"Test Publisher",supportEmail:"test@example.com",walletApiBaseUrl:"https://wallet-test.run.app",firebase:{apiKey:"synthetic",authDomain:"luma-drift-production.firebaseapp.com",projectId:"luma-drift-production",appId:"1:123:web:synthetic"}};' }));
  await context.route("https://www.gstatic.com/firebasejs/**/firebase-app.js", route => route.fulfill({ contentType:"text/javascript", headers:{"access-control-allow-origin":"*"}, body:'export const getApps=()=>[];export const initializeApp=()=>({name:"test"});' }));
  await context.route("https://www.gstatic.com/firebasejs/**/firebase-auth.js", route => route.fulfill({ contentType:"text/javascript", headers:{"access-control-allow-origin":"*"}, body:'export const getAuth=()=>({});export const inMemoryPersistence={};export const setPersistence=async()=>{};export class GoogleAuthProvider{setCustomParameters(){}};export const signInWithPopup=async()=>({user:{email:"synthetic@example.com",getIdToken:async()=>"synthetic-test-token"}});export const signOut=async()=>{};' }));
  let calls = 0;
  let response = { status:200, json:{deleted:true} };
  await context.route("https://wallet-test.run.app/v1/account", async route => {
    const request = route.request();
    if (request.method() === "OPTIONS") return route.fulfill({status:204,headers:{"access-control-allow-origin":origin,"access-control-allow-methods":"DELETE, OPTIONS","access-control-allow-headers":"Authorization, Content-Type"}});
    calls++;
    assert.equal(request.method(), "DELETE");
    assert.equal(request.headers().authorization, "Bearer synthetic-test-token");
    await route.fulfill({ status:response.status, contentType:"application/json", headers:{"access-control-allow-origin":origin}, body:JSON.stringify(response.json) });
  });
  await page.goto(`${origin}/delete-account/index.html?lang=en`);
  await page.locator("#google-sign-in").click();
  await page.locator("#delete-confirmation").waitFor({state:"visible"});
  assert.equal(await page.locator("#delete-account").isDisabled(), true);
  assert.equal(calls, 0);
  await page.locator("#confirm-delete").check();
  await page.locator("#delete-account").click();
  await page.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("have been deleted"));
  assert.equal(calls, 1);
  assert.equal(await page.locator("#delete-confirmation").isVisible(), false);
  findings.push("Synthetic web sign-in + explicit checkbox + authenticated DELETE + server-confirmed success");
  response = { status:401, json:{error:"recent-authentication-required"} };
  await page.reload();
  await page.locator("#google-sign-in").click();
  await page.locator("#confirm-delete").check();
  await page.locator("#delete-account").click();
  await page.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("Sign in again"));
  assert.equal(await page.locator("#delete-confirmation").isVisible(), false);
  findings.push("Synthetic expired-session rejection clears confirmation and requires reauthentication; no success claim");
  response = { status:200, json:{deleted:false} };
  await page.locator("#google-sign-in").click();
  await page.locator("#confirm-delete").check();
  await page.locator("#delete-account").click();
  await page.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("could not be confirmed"));
  findings.push("Synthetic invalid-success response does not announce deletion");
  await page.locator("#cancel-delete").click();
  await page.waitForFunction(() => document.querySelector("#deletion-status").textContent.includes("Sign in with Google"));
  const countBeforeCancel = calls;
  await page.locator("#google-sign-in").click();
  await page.locator("#cancel-delete").click();
  assert.equal(calls, countBeforeCancel);
  findings.push("Cancel signs out without sending a deletion request");
  await page.getByRole("button", {name:"FR",exact:true}).click();
  assert.match(await page.locator("[data-contact]").first().textContent(), /demande privée/);
  assert.equal(await page.locator('[data-contact] a[href^="mailto:"]').count(), 1);
  findings.push("Configured private email and FR/EN switching work without a public issue request route");
  console.log(JSON.stringify({status:"passed",findings,screenshots:output,realGoogleAuthenticationTested:false,liveServerDeletionTested:false},null,2));
} finally {
  if (browser) await browser.close();
  await new Promise(resolve => server.close(resolve));
}
