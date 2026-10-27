package com.androidhit.lumadrift.ui;

/** Physical-size-first canvas layout. Compact windows scroll instead of shrinking text. */
public final class ExperienceLayout {
    public static final float WIDTH = 390f;
    public final float scale, offsetX, viewportHeight, contentHeight, maximumScroll, minimumTextSize;
    public ExperienceLayout(int widthPixels, int heightPixels, int safeTop, int safeBottom, float density) {
        this(widthPixels, heightPixels, safeTop, safeBottom, density, 1f);
    }
    /** Keep a physical 12dp floor; requested larger fonts scale up to 1.3, not full adaptive text. */
    public ExperienceLayout(int widthPixels, int heightPixels, int safeTop, int safeBottom, float density, float fontScale) {
        float safeDensity = Math.max(.1f, Float.isFinite(density) ? density : 1f);
        float textScale = Math.max(1f, Math.min(1.3f, Float.isFinite(fontScale) ? fontScale : 1f));
        scale = Math.max(.1f, Math.min(widthPixels / WIDTH, safeDensity * 1.25f));
        offsetX = (widthPixels - WIDTH * scale) / 2f;
        viewportHeight = Math.max(1f, (heightPixels - safeTop - safeBottom) / scale);
        contentHeight = Math.max(840f, viewportHeight);
        maximumScroll = Math.max(0f, contentHeight - viewportHeight);
        minimumTextSize = 12f * safeDensity * textScale / scale;
    }
    public float clampScroll(float scroll) { return Math.max(0f, Math.min(maximumScroll, scroll)); }
    private ExperienceLayout() { throw new AssertionError(); }
}
