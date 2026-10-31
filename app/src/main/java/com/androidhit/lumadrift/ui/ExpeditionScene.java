package com.androidhit.lumadrift.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import com.androidhit.lumadrift.data.V118Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An illustrated expedition journal. This class only paints a supplied snapshot;
 * route decisions, random draws and saving belong to the host. Every action is a
 * world object with a free-standing caption, matching the 1.17 navigation.
 * Coordinates and returned touch targets use the caller's content rectangle.
 */
public final class ExpeditionScene {
    public enum Phase { START, ROUTE, RETURN }

    /** Localized branch copy; an empty future means that encounter is still hidden. */
    public static final class Branch {
        public String title = "", detail = "", reward = "", future = "";
        public Branch() {}
        public Branch(String title, String detail, String reward, String future) {
            this.title = safe(title); this.detail = safe(detail);
            this.reward = safe(reward); this.future = safe(future);
        }
    }

    /** Display data, with no references to the persistence or economy model. */
    public static final class Snapshot {
        public Phase phase = Phase.START;
        public String title = "", subtitle = "", companionName = "", rarity = "N";
        public String goalTitle = "", goalDetail = "", skillTitle = "", skillDetail = "";
        public String guaranteed = "", discovery = "";
        public int level = 1, copies = 1, nextCopies = 2, step = 1, steps = 4;
        public boolean skillAvailable = true, readyToPlay, claimed;
        public Branch left = new Branch(), right = new Branch(), third;
        public String[] receipt = new String[0];
    }

    /** Draw the current original portrait inside this art box. Never mutate saved state. */
    public interface PortraitRenderer { void draw(Canvas canvas, RectF art); }

    public static final class Target {
        public final String id, label;
        public final RectF bounds;
        public final boolean enabled;
        private Target(String id, String label, RectF bounds, boolean enabled) {
            this.id = id; this.label = label; this.bounds = bounds; this.enabled = enabled;
        }
    }

    public static final class Layout {
        public final List<Target> targets;
        public final RectF companionArt;
        public final float contentHeight;
        private Layout(List<Target> targets, RectF companionArt, float contentHeight) {
            this.targets = Collections.unmodifiableList(targets);
            this.companionArt = companionArt;
            this.contentHeight = contentHeight;
        }
        public Target target(String id) {
            for (Target target : targets) if (target.id.equals(id)) return target;
            return null;
        }
    }

    private static final float WIDTH = 390;
    private static final int INK = 0xff403451, MUTED = 0xff655573, TEAL = 0xff278e83;
    private static final int GOLD = 0xffad762d, VIOLET = 0xff7961ae, CREAM = 0xfffffbef;
    private final ScenicUi scenic = new ScenicUi();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint type = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float minimumText = 12, minimumTouch = 48, unit = 18;
    private String language = "en", pressedId = "";
    private RectF viewport;
    private float factor;
    private final ArrayList<Target> targets = new ArrayList<>();

    /** Height remains generous at large type; the host scrolls rather than shrinking text/art. */
    public static float requiredHeight(Snapshot supplied, float minimumText) {
        Snapshot s = supplied == null ? new Snapshot() : supplied;
        float line = Math.max(16, finite(minimumText) ? minimumText : 12) * 1.25f;
        float height = s.phase == Phase.ROUTE ? 236 + line * 3 + 16 : 292 + line * 5 + 22;
        float reward = Math.max(76, line * 3 + 12);
        if (s.phase == Phase.RETURN) {
            int receipts = Math.min(8, s.receipt == null ? 0 : s.receipt.length);
            return height + 146 + line * 2 + 13 + receipts * (line * 2 + 13)
                    + reward * 2 + 84 + 125 + 24;
        }
        height += line * 1.5f + (s.phase == Phase.ROUTE ? 110 : 211);
        if (s.phase == Phase.ROUTE && !s.readyToPlay) {
            height += line * 2 + 6;
            height += branchCount(s) * (line * 4.5f + 24);
        }
        height += line * (s.phase == Phase.ROUTE ? 5 : 4) + 38;
        height += (s.phase == Phase.ROUTE ? line * 4 + 16 : reward * 2) + 84;
        if (s.phase == Phase.START || s.readyToPlay) height += 106;
        return height + line * 2 + 24;
    }

    public Layout draw(Canvas canvas, RectF area, Snapshot supplied, String language,
                       float minimumText, float minimumTouch, String pressedId,
                       PortraitRenderer portrait) {
        Snapshot s = supplied == null ? new Snapshot() : supplied;
        if (canvas == null || area == null || area.width() <= 0 || !finite(area.width())) {
            return new Layout(new ArrayList<>(), new RectF(), 0);
        }
        this.viewport = new RectF(area);
        factor = area.width() / WIDTH;
        this.minimumText = Math.max(11, finite(minimumText) ? minimumText : 12);
        this.minimumTouch = Math.max(48, finite(minimumTouch) ? minimumTouch : 48);
        this.language = language == null ? "en" : language;
        this.pressedId = safe(pressedId);
        unit = Math.max(16, this.minimumText) * 1.25f;
        scenic.setMinimumTextSize(this.minimumText);
        targets.clear();
        canvas.save(); canvas.translate(area.left, area.top); canvas.scale(factor, factor);
        float height = requiredHeight(s, this.minimumText);
        drawAtmosphere(canvas, height);
        object(canvas, "expedition_back", new RectF(17, 25, 74, 84), "", ScenicUi.Kind.RETURN,
                VIOLET, text("back"), true);
        block(canvas, s.phase == Phase.ROUTE ? text("expedition") : empty(s.title) ? text(s.phase == Phase.RETURN ? "return_title" : "expedition") : s.title,
                88, 31, 278, 26, INK, true, 2);
        block(canvas, s.phase == Phase.ROUTE ? empty(s.title) ? text("observatory") : s.title
                : empty(s.subtitle) ? text("observatory") : s.subtitle,
                27, s.phase == Phase.ROUTE ? 88 : 100, 336, 15, MUTED, false, s.phase == Phase.ROUTE ? 1 : 2);

        boolean route = s.phase == Phase.ROUTE;
        RectF portraitArt = route ? new RectF(23, 112, 135, 224) : new RectF(22, 148, 150, 276);
        shadow(canvas, portraitArt.centerX(), portraitArt.bottom - 11, route ? 52 : 61, 12);
        if (portrait != null) portrait.draw(canvas, portraitArt);
        else scenic.object(canvas, portraitArt, VIOLET, false, ScenicUi.Kind.PROFILE);
        add("expedition_companion", text("choose_companion") + ": " + safe(s.companionName)
                + ". " + text("rarity", safe(s.rarity)) + ". " + text("level", Math.max(1, Math.min(10, s.level)))
                + ". " + (s.level >= 10 ? text("max_level") : text("copies", Math.max(0, s.copies), Math.max(1, s.nextCopies))),
                new RectF(portraitArt.left - 4, portraitArt.top - 7, portraitArt.right + 4, portraitArt.bottom + 4), s.phase == Phase.START);
        if (route) {
            block(canvas, s.companionName, 146, 115, 220, 21, INK, true, 1);
            block(canvas, text("rarity", safe(s.rarity)), 146, 148, 220, 13, VIOLET, true, 1);
            block(canvas, text("level", Math.max(1, Math.min(10, s.level))), 146, 174, 220, 15, INK, true, 1);
            block(canvas, s.level >= 10 ? text("max_level") : text("copies", Math.max(0, s.copies), Math.max(1, s.nextCopies)),
                    146, 201, 220, 12, MUTED, false, 1);
        } else companionProgress(canvas, new RectF(161, 150, 366, 282), s.companionName,
                s.rarity, s.level, s.copies, s.nextCopies);

        float y = route ? 236 : 292;
        scenic.object(canvas, new RectF(21, y + 7, 77, y + 67), TEAL, isPressed("expedition_goal"), ScenicUi.Kind.BLUEPRINT);
        block(canvas, text("goal"), 88, y, 276, 13, TEAL, true, 1);
        block(canvas, s.goalTitle, 88, y + unit, 276, 18, INK, true, 2);
        if (!route) block(canvas, s.goalDetail, 27, y + unit * 3, 336, 14, MUTED, false, 2);
        add("expedition_goal", text("choose_goal") + ": " + safe(s.goalTitle) + ". " + safe(s.goalDetail),
                new RectF(20, y - 5, 369, y + unit * (route ? 3 : 5)), s.phase == Phase.START);
        y += unit * (route ? 3 : 5) + (route ? 16 : 22);

        if (s.phase == Phase.RETURN) y = drawReturn(canvas, s, y);
        else {
            y = drawMap(canvas, s, y);
            if (route) y = drawSkill(canvas, s, y);
            if (s.phase == Phase.ROUTE && !s.readyToPlay) {
                block(canvas, text(branchCount(s) > 1 ? "route_choice" : "route_next"), 27, y, 336, 17, INK, true, 2);
                y += unit * 2 + 6;
                if (s.left != null) y = drawBranch(canvas, s.left, "expedition_left", y, ScenicUi.Kind.KEY, TEAL);
                if (s.right != null) y = drawBranch(canvas, s.right, "expedition_right", y, ScenicUi.Kind.STARS, VIOLET);
                if (s.third != null) y = drawBranch(canvas, s.third, "expedition_third", y, ScenicUi.Kind.LEAF, TEAL);
            }
            if (!route) y = drawSkill(canvas, s, y);
            if (route) {
                y = drawCompactReward(canvas, text("guaranteed"), s.guaranteed, y, TEAL);
                y = drawCompactReward(canvas, text("discovery"), s.discovery, y, VIOLET);
            } else {
                y = drawReward(canvas, text("guaranteed"), s.guaranteed, y, ScenicUi.Kind.CHEST, TEAL);
                y = drawReward(canvas, text("discovery"), s.discovery, y, ScenicUi.Kind.CAPSULE, VIOLET);
            }
            y = drawOdds(canvas, y);
            if (s.phase == Phase.START || s.readyToPlay) {
                String action = s.phase == Phase.START ? "expedition_start" : "expedition_continue";
                object(canvas, action, new RectF(80, y + 4, 310, y + 94),
                        text(s.phase == Phase.START ? "start" : "play_next"), ScenicUi.Kind.PLAY,
                        TEAL, text(s.phase == Phase.START ? "start" : "play_next"), true);
                y += 106;
            }
            block(canvas, text(s.phase == Phase.START ? "no_cost" : "route_saved"),
                    27, y, 336, 12, MUTED, false, 2);
            y += unit * 2;
        }
        canvas.restore();
        RectF externalPortrait = external(portraitArt);
        return new Layout(new ArrayList<>(targets), externalPortrait,
                Math.max(height, y + 24) * factor);
    }

    /** Standalone collection/detail variant with its own language and supported type floor. */
    public void companionProgress(Canvas canvas, RectF area, String name, String rarity,
                                  int level, int copies, int nextCopies, String language,
                                  float minimumText) {
        String previousLanguage = this.language;
        float previousMinimum = this.minimumText;
        this.language = language == null ? "en" : language;
        this.minimumText = Math.max(11, finite(minimumText) ? minimumText : 12);
        companionProgress(canvas, area, name, rarity, level, copies, nextCopies);
        this.language = previousLanguage;
        this.minimumText = previousMinimum;
    }

    /** A reusable detail row; the caller supplies an unchanging rarity and copy-derived level. */
    public void companionProgress(Canvas canvas, RectF area, String name, String rarity,
                                  int level, int copies, int nextCopies) {
        float w = area.width(), line = Math.max(18, minimumText) * 1.35f;
        block(canvas, safe(name), area.left, area.top, w, 23, INK, true, 1);
        block(canvas, text("rarity", safe(rarity)), area.left, area.top + line * 1.3f,
                w, 13, VIOLET, true, 1);
        block(canvas, text("level", Math.max(1, Math.min(10, level))), area.left, area.top + line * 2.35f,
                w, 16, INK, true, 1);
        float barY = area.top + line * 3.7f;
        paint.setColor(0xffddd5ea); paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(area.left, barY, area.right, barY + 5, 2.5f, 2.5f, paint);
        float ratio = level >= 10 ? 1 : Math.max(0, Math.min(1, copies / (float)Math.max(1, nextCopies)));
        paint.setColor(TEAL);
        canvas.drawRoundRect(area.left, barY, area.left + w * ratio, barY + 5, 2.5f, 2.5f, paint);
        block(canvas, level >= 10 ? text("max_level") : text("copies", Math.max(0, copies), Math.max(1, nextCopies)),
                area.left, barY + 11, w, 12, MUTED, false, 1);
    }

    private float drawMap(Canvas canvas, Snapshot s, float y) {
        block(canvas, text("route"), 27, y, 185, 18, INK, true, 1);
        block(canvas, text("step", Math.max(1, s.step), Math.max(1, s.steps)), 224, y + 2, 141, 14, MUTED, true, 1);
        float top = y + unit * 1.5f;
        if (s.phase == Phase.ROUTE) {
            float currentX = 48, forkX = 153, futureX = 259, homeX = 344;
            boolean fork = branchCount(s) > 1;
            float upper = top + (fork ? 18 : 46), lower = top + 75;
            trail(canvas, currentX, top + 46, forkX, upper, 0xffb8c9bd);
            trail(canvas, forkX, upper, futureX, upper, 0xffb8c9bd);
            trail(canvas, futureX, upper, homeX, top + 46, 0xffb8c9bd);
            if (fork) {
                trail(canvas, currentX, top + 46, forkX, lower, 0xffc8bce1);
                trail(canvas, forkX, lower, futureX, lower, 0xffc8bce1);
                trail(canvas, futureX, lower, homeX, top + 46, 0xffc8bce1);
            }
            scenic.object(canvas, new RectF(23, top + 19, 73, top + 69), GOLD, false, ScenicUi.Kind.COMPASS);
            scenic.object(canvas, new RectF(130, upper - 23, 176, upper + 23), TEAL, false, ScenicUi.Kind.KEY);
            farNode(canvas, futureX, upper, s.left == null ? "" : s.left.future, TEAL);
            if (fork) {
                scenic.object(canvas, new RectF(130, lower - 23, 176, lower + 23), VIOLET, false, ScenicUi.Kind.STARS);
                farNode(canvas, futureX, lower, s.right == null ? "" : s.right.future, VIOLET);
            }
            scenic.object(canvas, new RectF(homeX - 27, top + 19, homeX + 27, top + 73), GOLD, false, ScenicUi.Kind.HOUSE);
            return top + 110;
        }
        // The illustration follows the available proposals: entry/finale can
        // have just one path. A missing branch never creates a phantom action.
        boolean solo = branchCount(s) == 1;
        float leftX = solo ? 195 : 104, leftFutureX = solo ? 195 : 116;
        if (s.left != null) {
            trail(canvas, 195, top + 19, leftX, top + 70, 0xffb8c9bd);
            trail(canvas, leftX, top + 70, leftFutureX, top + 123, 0xffb8c9bd);
            trail(canvas, leftFutureX, top + 123, 195, top + 175, 0xffb8c9bd);
            scenic.object(canvas, new RectF(leftX - 39, top + 34, leftX + 39, top + 108),
                    TEAL, isPressed("expedition_left"), ScenicUi.Kind.KEY);
            farNode(canvas, leftFutureX, top + 123, s.left.future, TEAL);
        }
        if (s.right != null) {
            trail(canvas, 195, top + 19, 286, top + 70, 0xffc8bce1);
            trail(canvas, 286, top + 70, 274, top + 123, 0xffc8bce1);
            trail(canvas, 274, top + 123, 195, top + 175, 0xffc8bce1);
            scenic.object(canvas, new RectF(247, top + 34, 325, top + 108), VIOLET,
                    isPressed("expedition_right"), ScenicUi.Kind.STARS);
            farNode(canvas, 274, top + 123, s.right.future, VIOLET);
        }
        if (s.third != null) {
            trail(canvas, 195, top + 19, 195, top + 70, 0xff96baa4);
            trail(canvas, 195, top + 70, 195, top + 175, 0xff96baa4);
            scenic.object(canvas, new RectF(172, top + 48, 218, top + 94), TEAL, false, ScenicUi.Kind.LEAF);
        }
        scenic.object(canvas, new RectF(170, top - 9, 220, top + 41), GOLD, false, ScenicUi.Kind.COMPASS);
        scenic.object(canvas, new RectF(160, top + 142, 230, top + 206), GOLD, false, ScenicUi.Kind.HOUSE);
        return top + 211;
    }

    private void farNode(Canvas canvas, float x, float y, String revealed, int accent) {
        paint.setStyle(Paint.Style.FILL); paint.setColor(empty(revealed) ? 0xffe6dfef : 0xffe5f1e9);
        canvas.drawCircle(x, y, 19, paint);
        if (empty(revealed)) {
            paint.setTypeface(PulseUi124.face(800)); paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(Math.max(20, minimumText)); paint.setColor(MUTED);
            canvas.drawText("?", x, y + 7, paint); paint.setTextAlign(Paint.Align.LEFT);
        } else scenic.object(canvas, new RectF(x - 23, y - 25, x + 23, y + 21), accent, false, ScenicUi.Kind.LANTERN);
    }

    private float drawBranch(Canvas canvas, Branch supplied, String id, float y,
                             ScenicUi.Kind kind, int accent) {
        Branch b = supplied == null ? new Branch() : supplied;
        float rowHeight = unit * 4.5f + 12;
        scenic.object(canvas, new RectF(25, y + 2, 88, y + 68), accent, isPressed(id), kind);
        block(canvas, b.title, 102, y, 262, 18, INK, true, 1);
        block(canvas, b.detail, 102, y + unit * 1.25f, 262, 14, MUTED, false, 2);
        String future = empty(b.future) ? text("hidden_encounter") : text("revealed_encounter", b.future);
        block(canvas, future, 27, y + unit * 3.5f, 336, 12, accent, false, 1);
        add(id, safe(b.title) + ". " + safe(b.detail) + ". " + future + ". " + safe(b.reward),
                new RectF(22, y - 3, 368, y + rowHeight - 11), true);
        return y + rowHeight + 12;
    }

    private float drawSkill(Canvas canvas, Snapshot s, float y) {
        String title = empty(s.skillTitle) ? text("skill_reveal") : s.skillTitle;
        float skillHeight = unit * 4 + 24;
        scenic.object(canvas, new RectF(24, y + 5, 93, y + 78), VIOLET,
                isPressed("expedition_skill"), ScenicUi.Kind.EXPAND);
        block(canvas, text("skill"), 107, y, 257, 12, VIOLET, true, 1);
        block(canvas, title, 107, y + unit, 257, 18, INK, true, 1);
        block(canvas, s.skillDetail, 27, y + unit * 2.25f, 336, 14, MUTED, false, 2);
        boolean enabled = s.phase == Phase.ROUTE && !s.readyToPlay && s.skillAvailable;
        if (s.phase == Phase.ROUTE) {
            String status = text(s.skillAvailable ? "skill_ready" : "skill_used");
            block(canvas, status, 27, y + unit * 4.4f, 336, 13, enabled ? TEAL : MUTED, true, 1);
            skillHeight += unit;
            add("expedition_skill", status + ": " + title + ". " + safe(s.skillDetail),
                    new RectF(21, y - 4, 369, y + skillHeight - 10), enabled);
        }
        return y + skillHeight + 14;
    }

    private float drawReward(Canvas canvas, String title, String detail, float y,
                             ScenicUi.Kind kind, int accent) {
        scenic.object(canvas, new RectF(24, y + 5, 83, y + 65), accent, false, kind);
        block(canvas, title, 97, y + 2, 268, 13, accent, true, 1);
        block(canvas, detail, 97, y + unit + 2, 268, 15, INK, false, 2);
        return y + Math.max(76, unit * 3 + 12);
    }

    private float drawCompactReward(Canvas canvas, String title, String detail, float y, int accent) {
        block(canvas, title, 27, y, 147, 13, accent, true, 2);
        block(canvas, detail, 187, y, 178, 14, INK, false, 2);
        return y + unit * 2 + 8;
    }

    private float drawOdds(Canvas canvas, float y) {
        scenic.object(canvas, new RectF(26, y + 5, 83, y + 65), VIOLET,
                isPressed("expedition_odds"), ScenicUi.Kind.BOOK);
        block(canvas, text("odds"), 97, y + 15, 268, 14, VIOLET, true, 2);
        add("expedition_odds", text("odds") + ". " + text("discovery_note"),
                new RectF(22, y - 4, 368, y + 74), true);
        return y + 84;
    }

    private float drawReturn(Canvas canvas, Snapshot s, float y) {
        scenic.object(canvas, new RectF(126, y + 1, 264, y + 139), GOLD, false, ScenicUi.Kind.CHEST);
        y += 146;
        block(canvas, text(s.claimed ? "claimed" : "return_ready"), 27, y, 336, 15, TEAL, true, 2);
        y += unit * 2 + 13;
        if (s.receipt != null) for (int i = 0; i < Math.min(8, s.receipt.length); i++) {
            block(canvas, s.receipt[i], 35, y, 320, 17, INK, false, 2);
            y += unit * 2 + 13;
        }
        y = drawReward(canvas, text("guaranteed"), s.guaranteed, y, ScenicUi.Kind.CHEST, TEAL);
        y = drawReward(canvas, text("discovery"), s.discovery, y, ScenicUi.Kind.CAPSULE, VIOLET);
        y = drawOdds(canvas, y);
        object(canvas, "expedition_claim", new RectF(76, y + 8, 314, y + 110),
                text(s.claimed ? "return_home" : "claim_rewards"), s.claimed ? ScenicUi.Kind.HOUSE : ScenicUi.Kind.CHEST,
                TEAL, text(s.claimed ? "return_home" : "claim_rewards"), true);
        return y + 125;
    }

    private void object(Canvas canvas, String id, RectF art, String caption,
                        ScenicUi.Kind kind, int accent, String accessibility, boolean enabled) {
        scenic.objectAction(canvas, art, caption, accent, isPressed(id), kind);
        add(id, accessibility, art, enabled);
    }

    private void add(String id, String label, RectF raw, boolean enabled) {
        float cx = raw.centerX(), cy = raw.centerY();
        float w = Math.max(raw.width(), minimumTouch), h = Math.max(raw.height(), minimumTouch);
        RectF hit = new RectF(Math.max(0, cx - w / 2), Math.max(0, cy - h / 2),
                Math.min(WIDTH, cx + w / 2), cy + h / 2);
        targets.add(new Target(id, label, external(hit), enabled));
    }

    private RectF external(RectF local) {
        return new RectF(viewport.left + local.left * factor, viewport.top + local.top * factor,
                viewport.left + local.right * factor, viewport.top + local.bottom * factor);
    }

    private void block(Canvas canvas, String value, float x, float y, float width, float size,
                       int color, boolean heavy, int lines) {
        if (empty(value)) return;
        type.setColor(color); type.setTypeface(PulseUi124.face(heavy?800:400));
        type.setTextSize(Math.max(minimumText, size)); type.setShader(null);
        StaticLayout copy = StaticLayout.Builder.obtain(value, 0, value.length(), type, Math.max(1, (int) width))
                .setAlignment(Alignment.ALIGN_NORMAL).setIncludePad(false).setLineSpacing(0, 1.15f)
                .setMaxLines(lines).setEllipsize(TextUtils.TruncateAt.END).build();
        canvas.save(); canvas.translate(x, y); copy.draw(canvas); canvas.restore();
    }

    private void trail(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        path.reset(); path.moveTo(x1, y1); path.cubicTo(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2, x2, y2);
        paint.setColor(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(5); paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(path, paint); paint.setStyle(Paint.Style.FILL);
    }

    private void drawAtmosphere(Canvas canvas, float height) {
        paint.setColor(0x107fbdac); canvas.drawCircle(17, 410, 156, paint);
        paint.setColor(0x10b598d3); canvas.drawCircle(377, 606, 170, paint);
        for (int i = 0; i < 18; i++) {
            float x = 22 + (i * 79) % 346, y = 132 + (i * 137) % Math.max(1, (int)(height - 210));
            paint.setColor(i % 3 == 0 ? 0x32c2a165 : 0x22a99ac7);
            canvas.drawCircle(x, y, i % 3 == 0 ? 2 : 1.3f, paint);
        }
    }

    private void shadow(Canvas canvas, float x, float y, float rx, float ry) {
        paint.setColor(0x12665078); paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(x - rx, y - ry, x + rx, y + ry, paint);
    }

    private boolean isPressed(String id) { return pressedId.equals(id); }
    private String text(String key, Object... args) { return V118Strings.get(language, key, args); }
    private static String safe(String value) { return value == null ? "" : value; }
    private static boolean empty(String value) { return value == null || value.trim().isEmpty(); }
    private static int branchCount(Snapshot s) { return (s.left == null ? 0 : 1) + (s.right == null ? 0 : 1) + (s.third == null ? 0 : 1); }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
}
