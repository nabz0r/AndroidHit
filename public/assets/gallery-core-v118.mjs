export const LEVELS = Object.freeze([1, 3, 6, 10]);
export const POSES = Object.freeze({
  fr: ['Face', 'Dos', 'Gauche', 'Droite', 'Marche · face', 'Marche · dos', 'Marche · gauche', 'Marche · droite', 'Saut', 'Réception', 'Câlin', 'Accroupi', 'Heureux', 'Triste', 'Surpris', 'Pensif', 'Calme', 'Prêt', 'Garde', 'Fier'],
  en: ['Front', 'Back', 'Left', 'Right', 'Walking · front', 'Walking · back', 'Walking · left', 'Walking · right', 'Jump', 'Landing', 'Hug', 'Crouch', 'Happy', 'Sad', 'Surprised', 'Thinking', 'Calm', 'Ready', 'Guard', 'Proud']
});

export class BoundedCache {
  constructor(limit = 4) { this.limit = limit; this.values = new Map(); }
  async get(key, load) {
    if (this.values.has(key)) {
      const value = this.values.get(key); this.values.delete(key); this.values.set(key, value); return value;
    }
    const pending = Promise.resolve().then(load);
    this.values.set(key, pending);
    while (this.values.size > this.limit) this.values.delete(this.values.keys().next().value);
    try { return await pending; }
    catch (error) { if (this.values.get(key) === pending) this.values.delete(key); throw error; }
  }
}

export function validateSheet(sheet) {
  if (!sheet || !Array.isArray(sheet.frames) || sheet.frames.length !== 20) throw new Error('Invalid sheet');
  sheet.frames.forEach((frame, index) => {
    const r = frame.sourceRect;
    if (frame.poseIndex !== index || !Array.isArray(r) || r.length !== 4 || !r.every(Number.isFinite) || r[2] <= r[0] || r[3] <= r[1] || !Number.isFinite(frame.originX) || !Number.isFinite(frame.originY) || !(frame.pixelsPerUnit > 0)) throw new Error('Invalid frame');
    if (!Array.isArray(frame.sourceClip) || frame.sourceClip.length < 3 || frame.sourceClip.length > 256 || !frame.sourceClip.every(point => point.length === 2 && point.every(Number.isFinite) && point[0] >= r[0] && point[0] <= r[2] && point[1] >= r[1] && point[1] <= r[3])) throw new Error('Invalid extraction domain');
  });
  return sheet;
}

// One scale for the whole sheet keeps crouch, jump and standing poses proportional.
export function sheetViewport(sheet, width = 720, height = 720) {
  let left = 0, right = 0, top = 0, bottom = 0;
  for (const f of sheet.frames) {
    const [l, t, r, b] = f.sourceRect, u = f.pixelsPerUnit;
    left = Math.min(left, (l - f.originX) / u); right = Math.max(right, (r - f.originX) / u);
    top = Math.min(top, (t - f.originY) / u); bottom = Math.max(bottom, (b - f.originY) / u);
  }
  const scale = Math.min(165, (width - 72) / (right - left), (height - 72) / (bottom - top));
  return { scale, x: (width - (right + left) * scale) / 2, y: (height - (bottom + top) * scale) / 2 };
}

export function drawFrame(context, image, sheet, pose, width = 720, height = 720) {
  const frame = sheet.frames[pose], r = frame.sourceRect, u = frame.pixelsPerUnit;
  const view = sheetViewport(sheet, width, height);
  const point = (x, y) => [view.x + (x - frame.originX) / u * view.scale, view.y + (y - frame.originY) / u * view.scale];
  context.clearRect(0, 0, width, height); context.save(); context.beginPath();
  frame.sourceClip.forEach(([x, y], index) => { const p = point(x, y); if (index) context.lineTo(...p); else context.moveTo(...p); });
  context.closePath(); context.clip();
  const [x, y] = point(r[0], r[1]);
  context.drawImage(image, r[0], r[1], r[2] - r[0], r[3] - r[1], x, y, (r[2] - r[0]) / u * view.scale, (r[3] - r[1]) / u * view.scale);
  context.restore();
}
