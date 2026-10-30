import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { createRequire } from 'node:module';
import vm from 'node:vm';

const require = createRequire(import.meta.url);
const core = require('../lab-core.js');
const source = readFileSync(new URL('../lab.js', import.meta.url), 'utf8');

function fixture() {
  return {
    schemaVersion: 1,
    summary: {
      schemaVersion: 1,
      config: { profiles: 1, repetitions: 2, seed: 17 },
      totals: { trajectories: 4 },
      scenarios: ['current_model', 'teaching_support'].map((id) => ({
        id, label: id, trajectories: 2, completionRate: 0.5,
        blockedRate: 0.1, modeledD1: 0.5, modeledD7: 0,
        meanLevelsCleared: 2, meanActions: 30,
        meanSessionMinutes: 5, meanOverrunMinutes: 0.2,
      })),
      profileResults: [{ profileId: 'P001', scenario: 'current_model', trajectories: 2,
        completionRate: 0.5, meanLevelsCleared: 2 }],
    },
    profiles: { schemaVersion: 1, profiles: [{ id: 'P001', label: 'Profile 1',
      parameters: { planningDepth: 2, workingMemory: 4, attentionLapse: 0.1,
        learningRate: 0.2, rewardSensitivity: 0.5, frustrationSensitivity: 1.1 },
      iqLabelHypothesis: { value: 100, calibrated: false, causal: false } }] },
    pairedComparisons: { schemaVersion: 1, comparisons: [{
      scenario: 'teaching_support', baseline: 'current_model',
      metrics: { completionRate: { meanDifference: 0.1, ci95: [-0.1, 0.2], pairs: 2 } },
    }] },
    traces: { schemaVersion: 1, traces: [{ profileId: 'P001', scenario: 'current_model',
      repetition: 0, day: 0, level: 2, attempt: 1, step: 1,
      observation: { remaining: 2 }, decision: { action: 1, kind: 'tap_piece',
        reason: 'bounded_perceived_rules_depth_2', nodes: 2,
        candidates: [{ action: 1, value: 0.5 }] },
      outcome: { remaining: 1, fatigue: 0.1, frustration: 0 } }] },
  };
}

test('full engine report aliases normalize without altering payloads', () => {
  const report = fixture();
  const result = core.validate(report);
  assert.equal(result.paired, report.pairedComparisons);
  assert.equal(result.profiles, report.profiles);
  assert.equal(result.summary, report.summary);
  assert.equal(result.traces.traces[0].decision.candidates[0].value, 0.5);
});

test('summary-only imports keep absent sections empty, with no previous-run mixing', () => {
  const result = core.validate({ summary: fixture().summary });
  assert.deepEqual(result.profiles.profiles, []);
  assert.deepEqual(result.paired.comparisons, []);
  assert.deepEqual(result.traces.traces, []);
});

test('invalid schema, non-finite rates, duplicate and unknown identities are rejected', () => {
  const mutations = [
    (x) => { x.summary.schemaVersion = 2; },
    (x) => { x.summary.scope = { clinicalAssessment: true }; },
    (x) => { x.summary.scope = { humanParticipants: 100 }; },
    (x) => { x.summary.scope = { iqCalibration: true }; },
    (x) => { x.summary.scenarios[0].completionRate = Infinity; },
    (x) => { x.summary.scenarios[0].modeledD1 = 1.01; },
    (x) => { x.summary.scenarios[0].meanSessionMinutes = -1; },
    (x) => { x.summary.scenarios[0].trajectories = 1.2; },
    (x) => { x.summary.scenarios[1].id = x.summary.scenarios[0].id; },
    (x) => { x.traces.traces[0].scenario = 'unknown'; },
    (x) => { x.traces.traces[0].profileId = 'unknown'; },
    (x) => { x.traces.traces[0].decision.candidates[0].value = NaN; },
    (x) => { x.profiles.profiles[0].iqLabelHypothesis.calibrated = true; },
    (x) => { x.profiles.profiles[0].iqLabelHypothesis.causal = true; },
    (x) => { x.pairedComparisons.comparisons[0].metrics.completionRate.ci95 = [1, -1]; },
  ];
  for (const mutate of mutations) {
    const report = fixture(); mutate(report);
    assert.throws(() => core.validate(report), /invalidSchema/);
  }
  assert.throws(() => core.validate(JSON.parse('{"summary":{"schemaVersion":1},"__proto__":{}}')), /invalidSchema/);
});

test('deterministic chart bounds cannot leak invalid geometry', () => {
  assert.equal(core.barPercent(0.25, 1), 25);
  assert.equal(core.barPercent(2, 1), 100);
  assert.equal(core.barPercent(-1, 1), 0);
  for (const value of [undefined, null, NaN, Infinity, '0.5']) {
    assert.equal(core.barPercent(value, 1), 0);
  }
  assert.equal(core.barPercent(2, 0), 0);
});

test('duration formatting preserves nonzero overruns in seconds and actual zero', () => {
  assert.equal(core.formatDuration(0.03635, 'en'), '2.18 s');
  assert.equal(core.formatDuration(0.03635, 'fr'), '2,18 s');
  assert.equal(core.formatDuration(0, 'en'), '0 s');
  assert.equal(core.formatDuration(1.5, 'en'), '1.5 min');
  assert.equal(core.formatDuration(-0.03635, 'en'), '-2.18 s');
  assert.match(core.formatDuration(0.000001, 'en'), /6E-5 s/);
  assert.equal(core.formatDuration(NaN, 'en'), '—');
});

test('CSV keeps quotes, commas and Unicode; spreadsheet formulas are inert', () => {
  const csv = core.makeCSV([{ name: 'Mémoire, "A"', text: '=HYPERLINK("https://evil")', delta: -2 }]);
  assert.match(csv, /"Mémoire, ""A"""/);
  assert.match(csv, /"'=HYPERLINK/);
  assert.match(csv, /"-2"/);
  assert.equal(core.makeCSV([]), '');
});

// Minimal DOM contract: setters reject HTML so imported values must remain text.
class Element {
  constructor(tag = 'div') { this.tagName = tag; this.children = []; this.dataset = {}; this.style = {}; this.attrs = {}; this.events = {}; this.value = ''; this.hidden = false; this.checked = false; this._text = ''; }
  appendChild(child) { this.children.push(child); return child; }
  removeChild(child) { this.children.splice(this.children.indexOf(child), 1); }
  get firstChild() { return this.children[0]; }
  set textContent(value) { this._text = String(value); this.children = []; }
  get textContent() { return this._text + this.children.map((child) => child.textContent).join(''); }
  set innerHTML(_) { throw new Error('Unsafe HTML rendering'); }
  setAttribute(name, value) { this.attrs[name] = value; }
  addEventListener(name, callback) { this.events[name] = callback; }
}

async function runUI(report, options = {}) {
  const elements = new Map();
  const document = {
    documentElement: new Element('html'), title: '',
    querySelector(selector) { if (!elements.has(selector)) elements.set(selector, new Element()); return elements.get(selector); },
    querySelectorAll() { return []; },
    createElement: (tag) => new Element(tag),
    createTextNode: (value) => { const n = new Element('#text'); n.textContent = value; return n; },
  };
  let sequence = 0;
  class LocalURL extends URL {}
  LocalURL.createObjectURL = () => 'blob:test-' + (++sequence);
  LocalURL.revokeObjectURL = () => {};
  const window = { LumaCognitiveCore: core, COGNITIVE_LAB_SNAPSHOT: report,
    location: { href: options.url || 'https://example.test/lab/cognitive/' } };
  window.history = { replaceState: (_, __, href) => { window.location.href = href; } };
  const context = { window, document, navigator: { language: 'fr' },
    localStorage: { getItem: () => options.savedLanguage || 'fr', setItem: () => {} },
    URL: LocalURL, Blob, Intl, Map, Set,
    fetch: async () => { throw new Error('offline'); } };
  vm.runInNewContext(source, context, { timeout: 1500 });
  await new Promise((resolve) => setImmediate(resolve));
  return { elements, window, document };
}

test('small nonzero overruns remain visible in the rendered report', async () => {
  const report = fixture();
  report.summary.scenarios[0].meanOverrunMinutes = 0.03635;
  const rendered = await runUI(report);
  assert.match(rendered.elements.get('#outcomes-chart').textContent, /Dépassement du temps prévu: 2,18 s/);
  report.summary.scenarios[0].meanOverrunMinutes = 0;
  const zero = await runUI(report);
  assert.match(zero.elements.get('#outcomes-chart').textContent, /Dépassement du temps prévu: 0 s/);
});

test('explicit language query overrides saved language and preserves URL state', async () => {
  const rendered = await runUI(fixture(), { savedLanguage: 'en',
    url: 'https://example.test/lab/cognitive/?keep=a%20b&lang=fr#profiles' });
  assert.equal(rendered.document.documentElement.lang, 'fr');
  const url = new URL(rendered.window.location.href);
  assert.equal(url.searchParams.get('lang'), 'fr');
  assert.equal(url.searchParams.get('keep'), 'a b');
  assert.equal(url.hash, '#profiles');
  const english = await runUI(fixture(), { savedLanguage: 'fr',
    url: 'https://example.test/lab/cognitive/?lang=en#method' });
  assert.equal(english.document.documentElement.lang, 'en');
  assert.equal(new URL(english.window.location.href).hash, '#method');
});

test('real render keeps hostile imported strings as text and never HTML', async () => {
  const report = fixture();
  const malicious = '<img src=x onerror="globalThis.compromised=true">';
  report.profiles.profiles[0].label = malicious;
  report.summary.scenarios[0].label = malicious;
  report.traces.traces[0].decision.reason = malicious;
  const { elements } = await runUI(report);
  assert.equal(elements.get('#report').hidden, false);
  assert.ok(elements.get('#profile-rows').textContent.includes(malicious));
  assert.ok(elements.get('#selected-profile-title').textContent.includes(malicious));
  assert.ok(elements.get('#trace-content').textContent.includes(malicious));
  assert.equal(elements.get('#load-error').hidden, true);
});

test('retry traces remain separate samples and never merge their decisions', async () => {
  const report = fixture();
  report.traces.traces.push({ ...structuredClone(report.traces.traces[0]), attempt: 2 });
  const { elements } = await runUI(report);
  assert.equal(elements.get('#trace-select').children.length, 3); // prompt + attempts 1/2
});

test('complete published engine data passes the same importer contract when present', (context) => {
  const path = new URL('../../../../docs/validation/cognitive-lab/latest/report.json', import.meta.url);
  if (!existsSync(path)) { context.skip('Full report is not generated in this checkout.'); return; }
  if (existsSync(path)) {
    const result = core.validate(JSON.parse(readFileSync(path, 'utf8')));
    assert.ok(result.profiles.profiles.length >= 1);
    assert.equal(result.summary.scenarios.length, 3);
    assert.ok(result.traces.traces.length > 0);
  }
});
