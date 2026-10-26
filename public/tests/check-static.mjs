import fs from "node:fs/promises";
import path from "node:path";
import assert from "node:assert/strict";
import vm from "node:vm";
const root = path.resolve(new URL("..", import.meta.url).pathname);
const files = ["index.html", "privacy/index.html", "delete-account/index.html"];
let internalLinks = 0;
for (const name of files) {
  const source = await fs.readFile(path.join(root, name), "utf8");
  assert.match(source, /<html lang="fr">/);
  assert.match(source, /Content-Security-Policy/);
  assert.match(source, /name="referrer" content="no-referrer"/);
  assert.match(source, /data-set-lang="fr"/);
  assert.match(source, /data-set-lang="en"/);
  assert.match(source, /data-lang="fr"/);
  assert.match(source, /data-lang="en"/);
  assert.match(source, /id="main"/);
  assert.ok(!/\son[a-z]+\s*=/i.test(source), "No inline handlers");
  assert.ok(!/<script[^>]*>\s*[^<\s]/i.test(source), "No inline JavaScript");
  for (const [, reference] of source.matchAll(/(?:href|src)="([^"]+)"/g)) {
    if (/^(https?:|mailto:|#)/.test(reference)) continue;
    const target = path.resolve(path.dirname(path.join(root, name)), reference.split(/[?#]/)[0]);
    assert.ok(target.startsWith(root + path.sep));
    await fs.access(target);
    internalLinks++;
  }
}
const configSource = await fs.readFile(path.join(root, "site-config.js"), "utf8");
const sandbox = {window:{}};
vm.runInNewContext(configSource, sandbox);
const config = sandbox.window.LUMA_SITE_CONFIG;
const ads = (await fs.readFile(path.join(root,"app-ads.txt"),"utf8")).trim();
assert.equal(ads, "google.com, pub-6914185503996225, DIRECT, f08c47fec0942fa0");
if (process.argv.includes("--production")) {
  assert.equal(config.preparation, false, "Preparation mode must be explicitly reviewed and disabled");
  assert.ok(config.publisherName?.trim(), "Legal publisher name required");
  assert.match(config.supportEmail || "", /^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/);
  const {deletionConfiguration} = await import("../assets/deletion-core.mjs");
  assert.ok(deletionConfiguration(config), "Functional external deletion requires production web identity and backend configuration");
  assert.ok(config.walletApiBaseUrl.startsWith("https://"), "Production wallet API must use HTTPS");
}
console.log(JSON.stringify({status:"passed",htmlPages:files.length,internalLinks,adsPublisher:"pub-6914185503996225",preparationMode:config.preparation,productionReady:config.preparation===false},null,2));
