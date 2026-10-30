#!/usr/bin/env node
// Exercise the actual static report runtime, HTML IDs and frozen snapshot.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';
import { createHash } from 'node:crypto';
import vm from 'node:vm';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const web = resolve(root, 'public/lab/clarity-123');
const html = readFileSync(resolve(web, 'index.html'), 'utf8');
const runtime = readFileSync(resolve(web, 'report.js'), 'utf8');
const snapshot = readFileSync(resolve(web, 'data/snapshot.js'), 'utf8');
const data = JSON.parse(readFileSync(resolve(web, 'data/view.json'), 'utf8'));
const ids = ['manual_native_123', 'cause_hidden_123', 'loaned_mixed_123', 'energy_unlimited_123'];
class Element {
  constructor(tag = 'div') { this.tagName = tag; this.children = []; this.attributes = {}; this.dataset = {}; this.style = {}; this.hidden = false; this.value = ''; this.listeners = {}; this._text = ''; }
  set textContent(value) { this._text = String(value); this.children = []; }
  get textContent() { return this._text + this.children.map(x => x.textContent).join(' '); }
  append(...items) { this.children.push(...items); }
  replaceChildren(...items) { this._text = ''; this.children = items; }
  setAttribute(name, value) { this.attributes[name] = value; }
  addEventListener(name, handler) { this.listeners[name] = handler; }
  fire(name, value) { if (value !== undefined) this.value = value; this.listeners[name]?.({ target: this }); }
}
function boot(input = data, useSnapshot = false, search = '') {
  const elements = new Map();
  for (const [, tag, attributes] of html.matchAll(/<([a-z][a-z0-9-]*)\b([^>]*)>/gi)) {
    const id = /\bid="([^"]+)"/.exec(attributes)?.[1];
    const key = /\bdata-i18n="([^"]+)"/.exec(attributes)?.[1];
    if (!id && !key) continue;
    const element = new Element(tag); if (/\bhidden\b/.test(attributes)) element.hidden = true;
    if (key) element.dataset.i18n = key;
    elements.set(id ?? `i18n:${elements.size}`, element);
  }
  const document = { documentElement: { lang: 'fr' }, getElementById(id) { assert.ok(elements.has(id), `Missing actual HTML ID ${id}`); return elements.get(id); }, createElement: tag => new Element(tag), querySelectorAll(selector) { assert.equal(selector, '[data-i18n]'); return [...elements.values()].filter(e => e.dataset.i18n); } };
  const window = { location: { search }, ...(input === undefined ? {} : { CLARITY123: structuredClone(input) }) };
  const context = vm.createContext({ document, window, Intl, console, URLSearchParams });
  if (useSnapshot) vm.runInContext(snapshot, context);
  vm.runInContext(runtime, context);
  return { document, elements, get: id => document.getElementById(id), click: () => document.getElementById('language').fire('click'), choose: id => document.getElementById('condition').fire('change', id), window };
}
const texts = element => element.children.map(x => x.children.map(c => c.textContent));
const numbers = element => element.children.map(x => Number(x.children[0].textContent.replace(/[^0-9.-]/g, '')));

test('bundled snapshot is exactly the displayed JSON and matches run provenance', () => {
  const ui = boot(undefined, true);
  assert.deepEqual(JSON.parse(JSON.stringify(ui.window.CLARITY123)), data);
  const provenance = JSON.parse(readFileSync(resolve(web, 'data/provenance.json'), 'utf8'));
  assert.equal(createHash('sha256').update(readFileSync(resolve(web, 'data/provenance.json'))).digest('hex'), data.provenanceSha256);
  assert.equal(Object.keys(provenance.sourceSha256).length, 58);
  assert.equal(data.seed, 2026103002);
  assert.equal(data.counts.trajectories, 8000);
  assert.equal(data.counts.attemptRows, 380137);
  assert.equal(data.counts.coldProbes + data.counts.trainedProbes, 40000);
  assert.equal(data.counts.oracleBoardsSeparate, 104);
});
test('actual HTML initializes four French conditions and exact scope, with no human claim', () => {
  const ui = boot();
  assert.equal(ui.get('report').hidden, false);
  assert.equal(ui.document.documentElement.lang, 'fr');
  assert.deepEqual(ui.get('condition').children.map(e => e.value), ids);
  assert.deepEqual(numbers(ui.get('scope-counts')), [8000, 380137, 40000, 0]);
  assert.match(ui.get('status').textContent, /zéro humain/);
  assert.equal(ui.get('provenance-hash').textContent, data.provenanceSha256);
  for (const e of ui.elements.values()) if (e.dataset.i18n) assert.ok(e.textContent && !e.textContent.includes('undefined'), e.dataset.i18n);
});
test('English updates text, scope labels, links and condition labels', () => {
  const ui = boot(); ui.click();
  assert.equal(ui.document.documentElement.lang, 'en');
  assert.equal(ui.get('language').attributes['aria-label'], 'Passer en français');
  assert.equal(ui.get('downloads').attributes['aria-label'], 'Data and code');
  assert.equal(ui.get('scope-counts').attributes['aria-label'], 'Scope');
  assert.match(ui.get('status').textContent, /zero humans/);
  assert.match(ui.get('condition').children[3].textContent, /Unlimited energy/);
  for (const e of ui.elements.values()) if (e.dataset.i18n) assert.ok(e.textContent && !e.textContent.includes('undefined'), e.dataset.i18n);
});
test('English homepage link opens English directly and both home links retain chosen language', () => {
  const ui = boot(data, false, '?lang=en');
  assert.equal(ui.document.documentElement.lang, 'en');
  for (const id of ['brand-home', 'footer-home']) assert.equal(ui.get(id).attributes.href, 'https://luma-drift-production.web.app/?lang=en');
  ui.click(); assert.equal(ui.document.documentElement.lang, 'fr');
  for (const id of ['brand-home', 'footer-home']) assert.equal(ui.get(id).attributes.href, 'https://luma-drift-production.web.app/?lang=fr');
  assert.equal(boot(data, false, '?lang=unrecognized').document.documentElement.lang, 'fr');
});
test('every condition switch refreshes U27/U43/U48, resources, manual and automated counters', () => {
  const ui = boot(); ui.click();
  for (const id of [ids[3], ids[2], ids[1], ids[0]]) {
    ui.choose(id); const s = data.scenarios.find(x => x.id === id);
    assert.equal(ui.get('condition').value, id);
    assert.deepEqual(numbers(ui.get('assistance')), [s.manualExits, s.directCueManual, s.causeVisibleManual, s.automatedExits]);
    const resources = texts(ui.get('resources'));
    assert.equal(Number(resources[0][1].replace(/,/g, '')), s.energySpent);
    assert.equal(resources[1][1], `${s.energyEmpty.toLocaleString('en-US')} / 16,000`);
    assert.equal(resources[2][1], `${s.dayCap.toLocaleString('en-US')} / 16,000`);
    assert.deepEqual(texts(ui.get('levels')).map(r => [r[0], Number(r[1].replace(/,/g, ''))]), s.levels.map(x => [`U${x.level}`, x.starts]));
    assert.equal(ui.get('probes').children.findIndex(x => x.className === 'current'), ids.indexOf(id));
  }
});
test('rapid language and condition changes stay synchronous and retain all four conditions', () => {
  const ui = boot();
  for (let i = 0; i < 40; i++) { ui.choose(ids[i % 4]); ui.click(); assert.equal(ui.get('condition').value, ids[i % 4]); assert.equal(ui.get('condition').children.length, 4); }
  assert.equal(ui.document.documentElement.lang, 'fr');
  assert.deepEqual(numbers(ui.get('assistance')), [data.scenarios[3].manualExits, data.scenarios[3].directCueManual, data.scenarios[3].causeVisibleManual, 0]);
});
test('the single cold reference and four trained probe rows keep actual denominators', () => {
  const ui = boot(); ui.click();
  assert.match(ui.get('cold-reference').textContent, /4,587 \/ 8,000/);
  const rows = texts(ui.get('probes'));
  assert.equal(rows.length, 4);
  data.transfer.scenarios.forEach((s, i) => { assert.match(rows[i][1], new RegExp(`${s.completed.toLocaleString('en-US')} / 8,000`)); assert.equal(rows[i][2], `+${(s.completed - 4587).toLocaleString('en-US')}`); });
  assert.equal(data.transfer.coldProbeCount, 8000);
  assert.equal(data.transfer.trainedProbeCount, 32000);
  assert.equal(data.transfer.coldRowsReusedAsReferenceNotReexecuted, true);
});
test('invalid or absent snapshot fails closed and remains bilingual', () => {
  for (const input of [null, {}, { ...data, modelVersion: '1.22-old' }, { ...data, humanParticipants: 1 }, { ...data, scenarios: [] }, { ...data, counts: { ...data.counts, coldProbes: 32000 } }]) {
    const ui = boot(input); assert.equal(ui.get('report').hidden, true); assert.equal(ui.get('scope-counts').children.length, 0); assert.match(ui.get('status').textContent, /indisponible/); ui.click(); assert.match(ui.get('status').textContent, /unavailable/);
  }
});
test('partial numeric data or missing probe rows fail closed instead of showing mixed results', () => {
  for (const mutate of [x => { x.scenarios[1].starts = 'unknown'; }, x => { x.scenarios[0].levels = []; }, x => { delete x.transfer; }, x => { x.transfer.scenarios.pop(); }]) {
    const input = structuredClone(data); mutate(input);
    const ui = boot(input); assert.equal(ui.get('report').hidden, true); assert.match(ui.get('status').textContent, /indisponible/);
  }
});
test('invalid condition events cannot change the selected condition or counters', () => {
  const ui = boot(); const before = ui.get('assistance').textContent;
  ui.choose('cause_immediate_122');
  assert.equal(ui.get('assistance').textContent, before);
  ui.click(); assert.equal(ui.get('condition').value, ids[0]);
});
test('report has no async data fetch, import, oracle execution or archived dataset switch', () => {
  assert.doesNotMatch(runtime, /\b(?:fetch|XMLHttpRequest|solve|import)\s*\(/);
  assert.doesNotMatch(html, /id="(?:report-version|dataset)"/);
  assert.match(html, /104 contrôles oracle/);
  assert.match(html, /Zéro participant humain/);
});
