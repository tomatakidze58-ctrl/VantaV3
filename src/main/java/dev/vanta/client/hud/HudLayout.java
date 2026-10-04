package dev.vanta.client.hud;

import java.util.LinkedHashMap;
import java.util.Map;

/** Saved placement + appearance for each draggable HUD widget. */
public final class HudLayout {
    public enum Style {
        VANILLA("Vanilla"), CLEAN("Clean"), GLASS("Glass"), VANTA("Vanta");
        public final String label;
        Style(String label) { this.label = label; }
        public Style next() { Style[] v = values(); return v[(ordinal() + 1) % v.length]; }
        public static Style parse(String value, Style fallback) {
            if (value == null) return fallback;
            try { return valueOf(value.toUpperCase()); } catch (Exception ignored) { return fallback; }
        }
    }

    public record Pos(int x, int y) {}
    public record Appearance(float scale, boolean background, Style style) {}
    public record Size(int width, int height) {}

    private static final Map<String, Pos> POSITIONS = new LinkedHashMap<>();
    private static final Map<String, Appearance> APPEARANCE = new LinkedHashMap<>();
    private static final Map<String, Size> SIZES = new LinkedHashMap<>();

    static {
        register("stats", 8, 8, 138, 94, 1.00f, true, Style.CLEAN);
        register("saturation", 8, 108, 118, 27, 1.00f, false, Style.VANILLA);
        register("armor", 8, 146, 106, 31, 1.00f, false, Style.VANILLA);
        register("totems", 8, 184, 58, 24, 1.00f, false, Style.VANILLA);
        register("effects", 8, 220, 154, 128, 1.00f, true, Style.CLEAN);
        register("keys", 8, 352, 58, 38, 1.00f, true, Style.CLEAN);
    }

    private HudLayout() {}

    private static void register(String id, int x, int y, int width, int height, float scale, boolean background, Style style) {
        POSITIONS.put(id, new Pos(x, y));
        SIZES.put(id, new Size(width, height));
        APPEARANCE.put(id, new Appearance(scale, background, style));
    }

    public static Pos get(String id) { return POSITIONS.getOrDefault(id, new Pos(8, 8)); }
    public static void put(String id, int x, int y) {
        POSITIONS.put(id, new Pos(Math.max(0, x), Math.max(0, y)));
    }

    public static Size size(String id) { return SIZES.getOrDefault(id, new Size(100, 30)); }

    public static Appearance appearance(String id) {
        return APPEARANCE.getOrDefault(id, new Appearance(1f, true, Style.VANTA));
    }

    public static float scale(String id) { return appearance(id).scale(); }
    public static boolean background(String id) { return appearance(id).background(); }
    public static Style style(String id) { return appearance(id).style(); }

    public static void setScale(String id, float scale) {
        Appearance a = appearance(id);
        float clamped = Math.max(0.55f, Math.min(2.00f, Math.round(scale * 20f) / 20f));
        APPEARANCE.put(id, new Appearance(clamped, a.background(), a.style()));
    }

    public static void setBackground(String id, boolean background) {
        Appearance a = appearance(id);
        APPEARANCE.put(id, new Appearance(a.scale(), background, a.style()));
    }

    public static void setStyle(String id, Style style) {
        Appearance a = appearance(id);
        APPEARANCE.put(id, new Appearance(a.scale(), a.background(), style));
    }

    public static void cycleStyle(String id) { setStyle(id, style(id).next()); }

    public static Map<String, Pos> all() { return Map.copyOf(POSITIONS); }
    public static Map<String, Appearance> appearances() { return Map.copyOf(APPEARANCE); }
}
