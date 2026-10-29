import { BoundedCache, LEVELS, POSES, validateSheet, drawFrame } from './gallery-core-v118.mjs';

const root = document.querySelector('[data-companion-gallery]');
if (root) {
  const $ = name => root.querySelector(`[data-gallery-${name}]`);
  const cache = new BoundedCache(4), metadata = new BoundedCache(4);
  let opened = false, request = 0, language = document.documentElement.lang === 'en' ? 'en' : 'fr';
  const copy = {
    fr: { loading: 'Le compagnon arrive…', error: 'Cet aperçu n’a pas pu être chargé. Réessaie.', form: 'Forme', level: 'niveau visuel', original: 'Collection Luma', guest: 'Série invitée Mini LoL', ready: 'Aperçu chargé', retry: 'Réessayer' },
    en: { loading: 'Your companion is on the way…', error: 'This preview could not load. Please try again.', form: 'Form', level: 'visual level', original: 'Luma collection', guest: 'Mini LoL guest series', ready: 'Preview loaded', retry: 'Try again' }
  };
  const image = path => new Promise((resolve, reject) => { const value = new Image(); value.onload = () => resolve(value); value.onerror = reject; value.src = path; });
  function translate() {
    language = document.documentElement.lang === 'en' ? 'en' : 'fr';
    [...$('pose').options].forEach((option, index) => { option.textContent = `${String(index + 1).padStart(2, '0')} · ${POSES[language][index]}`; });
    [...$('form').options].forEach((option, index) => { option.textContent = `${copy[language].form} ${index + 1} · ${copy[language].level} ${LEVELS[index]}`; });
    root.querySelectorAll('[data-gallery-stance]').forEach(button => { button.textContent = POSES[language][Number(button.dataset.galleryStance)]; });
    if (opened) render();
  }
  async function render() {
    if (!opened) return;
    const token = ++request, id = $('character').value, form = Number($('form').value), pose = Number($('pose').value);
    const who = $('character').selectedOptions[0].textContent;
    $('status').textContent = copy[language].loading; root.setAttribute('aria-busy', 'true');
    root.querySelectorAll('[data-gallery-stance]').forEach(button => button.setAttribute('aria-pressed', String(Number(button.dataset.galleryStance) === pose)));
    try {
      const path = `assets/companions-v118/${id}-evo${String(LEVELS[form]).padStart(2, '0')}-poses`;
      const sheet = await metadata.get(path, async () => { const response = await fetch(path + '.json'); if (!response.ok) throw new Error('metadata'); return validateSheet(await response.json()); });
      const atlas = await cache.get(path, () => image(path + '.webp'));
      if (token !== request) return;
      drawFrame($('canvas').getContext('2d'), atlas, sheet, pose);
      $('canvas').hidden = false; $('placeholder').hidden = true;
      $('canvas').setAttribute('aria-label', `${who}. ${POSES[language][pose]}. ${copy[language].form} ${form + 1} / 4, ${copy[language].level} ${LEVELS[form]}.`);
      $('name').textContent = who; $('group').textContent = id.startsWith('lol_') ? copy[language].guest : copy[language].original;
      $('status').textContent = `${copy[language].ready} · ${who} · ${POSES[language][pose]}`;
      $('load').hidden = true;
    } catch (_) {
      if (token !== request) return;
      $('status').textContent = copy[language].error; $('load').hidden = false; $('load').textContent = copy[language].retry;
    } finally { if (token === request) root.setAttribute('aria-busy', 'false'); }
  }
  $('load').addEventListener('click', () => { opened = true; render(); });
  for (const name of ['character', 'form', 'pose']) $(name).addEventListener('change', () => { opened = true; render(); });
  for (const [name, offset] of [['previous', -1], ['next', 1]]) $(name).addEventListener('click', () => { opened = true; $('pose').value = (Number($('pose').value) + offset + 20) % 20; render(); });
  root.querySelectorAll('[data-gallery-stance]').forEach(button => button.addEventListener('click', () => { opened = true; $('pose').value = button.dataset.galleryStance; render(); }));
  window.addEventListener('luma-language', translate);
  translate();
  // No image, metadata or atlas is requested until an actual gallery choice.
}
