package dev.vanta.client.hud;

import dev.vanta.client.VantaClient;
import dev.vanta.client.util.Reflect;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class VantaHud {
    private static final Map<String, float[]> CURRENT = new HashMap<>();
    private static final Map<String, Integer> EFFECT_MAX = new HashMap<>();
    private static boolean zoomHeld;
    private static Double oldFov;

    private VantaHud() {}
    public static void setZoomHeld(boolean held) { zoomHeld = held; }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        applyZoom(mc);

        if (hasStats()) widget(g, "stats", () -> drawStats(g, mc));
        if (VantaClient.MODULES.on("Saturation HUD")) widget(g, "saturation", () -> drawSaturation(g, mc));
        if (VantaClient.MODULES.on("Armor HUD")) widget(g, "armor", () -> drawArmor(g, mc));
        if (VantaClient.MODULES.on("Totem Counter")) widget(g, "totems", () -> drawTotems(g, mc));
        if (VantaClient.MODULES.on("Status Effect Bars")) widget(g, "effects", () -> drawEffects(g, mc));
        if (VantaClient.MODULES.on("Keystrokes")) widget(g, "keys", () -> drawKeys(g, mc));
        if (VantaClient.MODULES.on("Crosshair")) drawCrosshair(g, mc);
    }

    private static boolean hasStats() {
        return VantaClient.MODULES.on("FPS Counter") || VantaClient.MODULES.on("Ping Display")
                || VantaClient.MODULES.on("Coordinates") || VantaClient.MODULES.on("Direction")
                || VantaClient.MODULES.on("CPS Counter") || VantaClient.MODULES.on("Memory Usage")
                || VantaClient.MODULES.on("Clock") || VantaClient.MODULES.on("Server Address")
                || VantaClient.MODULES.on("Player Count");
    }

    private static void widget(GuiGraphicsExtractor g, String id, Runnable draw) {
        int[] p = pos(id);
        float scale = HudLayout.scale(id);
        g.pose().pushMatrix();
        g.pose().translate(p[0], p[1]);
        g.pose().scale(scale, scale);
        draw.run();
        g.pose().popMatrix();
    }

    private static int[] pos(String id) {
        HudLayout.Pos target = HudLayout.get(id);
        float[] p = CURRENT.computeIfAbsent(id, k -> new float[]{target.x(), target.y()});
        float speed = VantaSettings.smoothHud ? 0.22f : 1f;
        p[0] += (target.x() - p[0]) * speed;
        p[1] += (target.y() - p[1]) * speed;
        return new int[]{Math.round(p[0]), Math.round(p[1])};
    }

    private static void drawStats(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "stats";
        int y = 0;
        if (VantaClient.MODULES.on("FPS Counter")) y = infoRow(g, mc, id, y, "FPS", Integer.toString(fps(mc)));
        if (VantaClient.MODULES.on("Ping Display")) y = infoRow(g, mc, id, y, "PING", ping(mc) + " ms");
        if (VantaClient.MODULES.on("Coordinates")) y = infoRow(g, mc, id, y, "XYZ", String.format("%.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (VantaClient.MODULES.on("Direction")) y = infoRow(g, mc, id, y, "DIR", direction(mc.player.getYRot()));
        if (VantaClient.MODULES.on("CPS Counter")) y = infoRow(g, mc, id, y, "CPS", Integer.toString(VantaClient.cps()));
        if (VantaClient.MODULES.on("Memory Usage")) {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
            long max = r.maxMemory() / 1024 / 1024;
            y = infoRow(g, mc, id, y, "RAM", used + "/" + max + " MB");
        }
        if (VantaClient.MODULES.on("Clock")) y = infoRow(g, mc, id, y, "TIME", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (VantaClient.MODULES.on("Server Address")) y = infoRow(g, mc, id, y, "SERVER", server(mc));
        if (VantaClient.MODULES.on("Player Count")) infoRow(g, mc, id, y, "PLAYERS", playerCount(mc));
    }

    private static int infoRow(GuiGraphicsExtractor g, Minecraft mc, String id, int y, String label, String value) {
        int w = Math.max(104, mc.font.width(label + "  " + value) + 14);
        panel(g, id, 0, y, w, 15);
        int accent = accent(id), text = text(id);
        if (HudLayout.style(id) != HudLayout.Style.VANILLA) g.fill(0, y, 2, y + 15, accent);
        g.text(mc.font, label, 6, y + 3, accent, true);
        g.text(mc.font, value, 9 + mc.font.width(label), y + 3, text, false);
        return y + 17;
    }

    /** Food/saturation overlay with the same information goal as AppleSkin, drawn with original Vanta shapes. */
    private static void drawSaturation(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "saturation";
        int food = mc.player.getFoodData().getFoodLevel();
        float sat = mc.player.getFoodData().getSaturationLevel();
        float exhaustion = 0f;
        Object ex = Reflect.call(mc.player.getFoodData(), "getExhaustionLevel");
        if (ex instanceof Number n) exhaustion = n.floatValue();

        panel(g, id, 0, 0, 118, 27);
        int accent = accent(id), muted = muted(id), text = text(id);
        if (HudLayout.style(id) != HudLayout.Style.VANILLA) {
            g.text(mc.font, "FOOD", 5, 3, muted, false);
            g.text(mc.font, food + " / " + String.format("%.1f", sat), 65, 3, text, false);
        }

        int py = HudLayout.style(id) == HudLayout.Style.VANILLA ? 4 : 15;
        for (int i = 0; i < 10; i++) {
            int x = i * 11 + 4;
            int hunger = Math.max(0, Math.min(2, food - i * 2));
            float saturation = Math.max(0f, Math.min(2f, sat - i * 2f));
            g.outline(x, py, 9, 9, HudLayout.style(id) == HudLayout.Style.VANILLA ? 0xAA4B3A31 : 0x884A3B52);
            if (hunger > 0) {
                int h = hunger == 2 ? 7 : 4;
                g.fill(x + 1, py + 8 - h, x + 8, py + 8, 0xFFE86B3A);
            }
            if (saturation > 0f) {
                int h = Math.max(1, Math.round((saturation / 2f) * 7f));
                g.fill(x + 2, py + 8 - h, x + 7, py + 8, accent);
            }
        }
        int exW = Math.min(108, Math.round((exhaustion / 4f) * 108f));
        if (exW > 0) g.fill(5, py + 11, 5 + exW, py + 13, 0xFFFFC46B);
    }

    private static void drawArmor(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "armor";
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        panel(g, id, 0, 0, 106, 31);
        if (HudLayout.style(id) != HudLayout.Style.VANILLA) g.text(mc.font, "ARMOR", 5, 3, accent(id), true);
        int baseY = HudLayout.style(id) == HudLayout.Style.VANILLA ? 2 : 12;
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = mc.player.getItemBySlot(slots[i]);
            int ix = 4 + i * 25;
            if (!s.isEmpty()) {
                try { g.item(s, ix, baseY); } catch (Throwable ignored) {}
                if (s.isDamageableItem()) {
                    int left = Math.max(0, s.getMaxDamage() - s.getDamageValue());
                    int pct = s.getMaxDamage() == 0 ? 100 : Math.round(left * 100f / s.getMaxDamage());
                    int c = pct <= 15 ? 0xFFFF5D73 : pct <= 35 ? 0xFFFFCA5C : 0xFF9CFFB2;
                    g.text(mc.font, Integer.toString(pct), ix + 3, baseY + 16, c, true);
                }
            } else if (HudLayout.style(id) != HudLayout.Style.VANILLA) {
                g.outline(ix, baseY, 16, 16, 0x553D2B50);
            }
        }
    }

    private static void drawTotems(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "totems";
        int count = countTotems(mc);
        panel(g, id, 0, 0, 58, 24);
        ItemStack icon = new ItemStack(Items.TOTEM_OF_UNDYING);
        try { g.item(icon, 3, 4); } catch (Throwable ignored) {}
        g.text(mc.font, "x" + count, 25, 8, count > 0 ? text(id) : muted(id), true);
    }

    private static void drawEffects(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "effects";
        int shown = 0;
        for (Object inst : mc.player.getActiveEffects()) {
            if (shown >= 6) break;
            int duration = Reflect.intCall(inst, "getDuration", 0);
            Object holder = Reflect.call(inst, "getEffect");
            Object effect = Reflect.call(holder, "value");
            Object display = Reflect.call(effect, "getDisplayName");
            String name = display instanceof Component c ? c.getString() : "Effect";
            int amplifier = Reflect.intCall(inst, "getAmplifier", 0);
            String key = name + ":" + amplifier;
            int max = Math.max(duration, EFFECT_MAX.getOrDefault(key, duration));
            EFFECT_MAX.put(key, max);
            float ratio = max <= 0 ? 1f : Math.max(0f, Math.min(1f, duration / (float) max));
            int yy = shown * 21;
            panel(g, id, 0, yy, 154, 19);
            String level = amplifier > 0 ? " " + (amplifier + 1) : "";
            g.text(mc.font, name + level, 5, yy + 3, text(id), false);
            String time = durationText(duration);
            g.text(mc.font, time, 149 - mc.font.width(time), yy + 3, muted(id), false);
            g.fill(5, yy + 14, 149, yy + 17, 0x55211928);
            g.fill(5, yy + 14, 5 + Math.round(144 * ratio), yy + 17, accent(id));
            shown++;
        }
    }

    private static String durationText(int ticks) {
        int sec = Math.max(0, ticks / 20);
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    private static void drawKeys(GuiGraphicsExtractor g, Minecraft mc) {
        String id = "keys";
        boolean w = Reflect.keyDown(mc.options, "keyUp"), a = Reflect.keyDown(mc.options, "keyLeft");
        boolean s = Reflect.keyDown(mc.options, "keyDown"), d = Reflect.keyDown(mc.options, "keyRight");
        key(g, mc, id, 20, 0, "W", w);
        key(g, mc, id, 0, 20, "A", a);
        key(g, mc, id, 20, 20, "S", s);
        key(g, mc, id, 40, 20, "D", d);
    }

    private static void key(GuiGraphicsExtractor g, Minecraft mc, String id, int x, int y, String t, boolean down) {
        int base = HudLayout.background(id) ? panelColor(id) : 0x00000000;
        if (HudLayout.background(id)) g.fill(x, y, x + 18, y + 18, down ? accent(id) : base);
        g.outline(x, y, 18, 18, down ? accentBright(id) : border(id));
        g.text(mc.font, t, x + 6, y + 5, down ? 0xFFFFFFFF : accent(id), true);
    }

    private static void drawCrosshair(GuiGraphicsExtractor g, Minecraft mc) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2, cy = mc.getWindow().getGuiScaledHeight() / 2;
        int c = 0xFFB15CFF;
        g.fill(cx - 7, cy, cx - 2, cy + 1, c);
        g.fill(cx + 3, cy, cx + 8, cy + 1, c);
        g.fill(cx, cy - 7, cx + 1, cy - 2, c);
        g.fill(cx, cy + 3, cx + 1, cy + 8, c);
    }

    private static void panel(GuiGraphicsExtractor g, String id, int x, int y, int w, int h) {
        if (!HudLayout.background(id)) return;
        HudLayout.Style style = HudLayout.style(id);
        if (style == HudLayout.Style.VANILLA) {
            g.fill(x, y, x + w, y + h, 0x66000000);
        } else {
            g.fill(x, y, x + w, y + h, panelColor(id));
            g.outline(x, y, w, h, border(id));
        }
    }

    private static int panelColor(String id) {
        return switch (HudLayout.style(id)) {
            case VANILLA -> 0x66000000;
            case CLEAN -> 0xCC111217;
            case GLASS -> 0x99100E17;
            case VANTA -> 0xD90D0816;
        };
    }
    private static int border(String id) {
        return switch (HudLayout.style(id)) {
            case VANILLA -> 0x44FFFFFF;
            case CLEAN -> 0x664A4D55;
            case GLASS -> 0x77746386;
            case VANTA -> 0xAA7C38C5;
        };
    }
    private static int accent(String id) {
        return switch (HudLayout.style(id)) {
            case VANILLA -> 0xFFFFFFFF;
            case CLEAN -> 0xFFD3D6DC;
            case GLASS -> 0xFFD0B9E9;
            case VANTA -> 0xFFB15CFF;
        };
    }
    private static int accentBright(String id) { return HudLayout.style(id) == HudLayout.Style.VANTA ? 0xFFE0B4FF : accent(id); }
    private static int text(String id) { return HudLayout.style(id) == HudLayout.Style.VANILLA ? 0xFFFFFFFF : 0xFFF6F0FF; }
    private static int muted(String id) { return HudLayout.style(id) == HudLayout.Style.VANILLA ? 0xFFBFBFBF : 0xFFAA9CB8; }

    private static int fps(Minecraft mc) { return Reflect.intCall(mc, "getFps", 0); }
    private static int ping(Minecraft mc) {
        Object conn = Reflect.call(mc, "getConnection"), info = conn == null ? null : Reflect.call(conn, "getPlayerInfo", mc.player.getUUID());
        int p = Reflect.intCall(info, "getLatency", -1);
        if (p < 0) p = Reflect.intCall(info, "getPing", 0);
        return Math.max(0, p);
    }
    private static String server(Minecraft mc) {
        Object s = Reflect.call(mc, "getCurrentServer"), ip = s == null ? null : Reflect.field(s, "ip");
        return ip == null ? "Singleplayer" : String.valueOf(ip);
    }
    private static String playerCount(Minecraft mc) {
        Object c = Reflect.call(mc, "getConnection"), l = c == null ? null : Reflect.call(c, "getOnlinePlayers");
        return l instanceof java.util.Collection<?> col ? Integer.toString(col.size()) : "1";
    }
    private static int countTotems(Minecraft mc) {
        int count = 0;
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == Items.TOTEM_OF_UNDYING) count += s.getCount();
        }
        return count;
    }
    private static String direction(float yaw) {
        return switch (Math.floorMod(Math.round(yaw / 90f), 4)) {
            case 0 -> "South"; case 1 -> "West"; case 2 -> "North"; default -> "East";
        };
    }

    private static void applyZoom(Minecraft mc) {
        if (!VantaClient.MODULES.on("Zoom")) { restoreFov(mc); return; }
        Object current = Reflect.getOption(mc.options, "fov");
        if (!(current instanceof Number n)) return;
        if (zoomHeld) {
            if (oldFov == null) oldFov = n.doubleValue();
            int now = n.intValue(), target = VantaSettings.zoomFov;
            int next = Math.max(target, now - Math.max(1, (now - target) / 3));
            Reflect.setOption(mc.options, next, "fov");
        } else restoreFov(mc);
    }
    private static void restoreFov(Minecraft mc) {
        if (oldFov != null) {
            Reflect.setOption(mc.options, oldFov.intValue(), "fov");
            oldFov = null;
        }
    }
}
