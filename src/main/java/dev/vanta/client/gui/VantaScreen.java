package dev.vanta.client.gui;

import dev.vanta.client.VantaClient;
import dev.vanta.client.hud.HudLayout;
import dev.vanta.client.module.Module;
import dev.vanta.client.util.Config;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Prestige-inspired layout rebuilt from scratch for Vanta: sidebar, module list, settings panel. */
public final class VantaScreen extends Screen {
    private Module.Category selectedCategory;
    private String selectedModuleName;

    public VantaScreen() { this(Module.Category.HUD, "Saturation HUD"); }
    private VantaScreen(Module.Category category, String module) {
        super(Component.literal("Vanta Client"));
        this.selectedCategory = category;
        this.selectedModuleName = module;
    }

    @Override
    protected void init() {
        int panelW = Math.min(1120, width - 28);
        int panelH = Math.min(660, height - 28);
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int sidebarW = 172;
        int rightW = 282;
        int contentX = px + sidebarW + 24;
        int rightX = px + panelW - rightW - 14;
        int centerW = rightX - contentX - 12;

        int cy = py + 92;
        for (Module.Category category : Module.Category.values()) {
            String prefix = category == selectedCategory ? "◆ " : "◇ ";
            this.addRenderableWidget(Button.builder(Component.literal(prefix + label(category)), b ->
                    this.minecraft.gui.setScreen(new VantaScreen(category, firstModuleName(category))))
                    .bounds(px + 14, cy, sidebarW - 28, 24).build());
            cy += 31;
        }

        List<Module> mods = VantaClient.MODULES.in(selectedCategory);
        int cardGap = 7;
        int cardW = Math.max(170, (centerW - cardGap) / 2);
        int startY = py + 104;
        for (int i = 0; i < mods.size(); i++) {
            Module module = mods.get(i);
            int col = i % 2, row = i / 2;
            int x = contentX + col * (cardW + cardGap);
            int y = startY + row * 32;
            if (y > py + panelH - 48) break;
            int toggleW = 45;
            this.addRenderableWidget(Button.builder(Component.literal(module.name), b ->
                    this.minecraft.gui.setScreen(new VantaScreen(selectedCategory, module.name)))
                    .bounds(x, y, cardW - toggleW - 3, 25).build());
            this.addRenderableWidget(Button.builder(Component.literal(module.enabled ? "ON" : "OFF"), b -> {
                VantaClient.MODULES.toggle(module);
                this.minecraft.gui.setScreen(new VantaScreen(selectedCategory, module.name));
            }).bounds(x + cardW - toggleW, y, toggleW, 25).build());
        }

        Module selected = selectedModule();
        if (selected == null) return;
        int bx = rightX + 14, bw = rightW - 28;
        this.addRenderableWidget(Button.builder(Component.literal(selected.enabled ? "Disable module" : "Enable module"), b -> {
            VantaClient.MODULES.toggle(selected);
            this.minecraft.gui.setScreen(new VantaScreen(selectedCategory, selected.name));
        }).bounds(bx, py + 168, bw, 24).build());

        String widget = widgetId(selected.name);
        if (widget != null) {
            this.addRenderableWidget(Button.builder(Component.literal("Size −"), b -> {
                HudLayout.setScale(widget, HudLayout.scale(widget) - 0.05f); saveAndRefresh(selected);
            }).bounds(bx, py + 244, (bw - 6) / 2, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("Size +"), b -> {
                HudLayout.setScale(widget, HudLayout.scale(widget) + 0.05f); saveAndRefresh(selected);
            }).bounds(bx + (bw + 6) / 2, py + 244, (bw - 6) / 2, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("Style: " + HudLayout.style(widget).label), b -> {
                HudLayout.cycleStyle(widget); saveAndRefresh(selected);
            }).bounds(bx, py + 272, bw, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("Background: " + (HudLayout.background(widget) ? "ON" : "OFF")), b -> {
                HudLayout.setBackground(widget, !HudLayout.background(widget)); saveAndRefresh(selected);
            }).bounds(bx, py + 300, bw, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("Open HUD editor"), b ->
                    this.minecraft.gui.setScreen(new HudEditorScreen(widget)))
                    .bounds(bx, py + 328, bw, 22).build());
        } else if (hasScalarSetting(selected.name)) {
            this.addRenderableWidget(Button.builder(Component.literal("−"), b -> {
                adjustSetting(selected.name, -1); saveAndRefresh(selected);
            }).bounds(bx, py + 246, 50, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                adjustSetting(selected.name, 1); saveAndRefresh(selected);
            }).bounds(rightX + rightW - 64, py + 246, 50, 22).build());
        }

        if (selected.name.equals("HUD Editor")) {
            this.addRenderableWidget(Button.builder(Component.literal("Open HUD editor"), b -> this.minecraft.gui.setScreen(new HudEditorScreen()))
                    .bounds(bx, py + 244, bw, 24).build());
            this.addRenderableWidget(Button.builder(Component.literal("GUI preset: " + guiStyleLabel()), b -> {
                cycleGuiStyle(); saveAndRefresh(selected);
            }).bounds(bx, py + 274, bw, 22).build());
        }
    }

    private void saveAndRefresh(Module selected) {
        Config.save(VantaClient.MODULES);
        this.minecraft.gui.setScreen(new VantaScreen(selectedCategory, selected.name));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int panelW = Math.min(1120, width - 28);
        int panelH = Math.min(660, height - 28);
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int sidebarW = 172, rightW = 282;
        int contentX = px + sidebarW + 24;
        int rightX = px + panelW - rightW - 14;

        int shell = guiShell(), panel = guiPanel(), accent = guiAccent(), border = guiBorder();
        g.fill(0, 0, width, height, 0x77000000);
        g.fill(px, py, px + panelW, py + panelH, shell);
        g.outline(px, py, panelW, panelH, border);
        g.fillGradient(px, py, px + panelW, py + 68, guiHeaderTop(), shell);
        g.fill(px + 10, py + 76, px + sidebarW, py + panelH - 10, panel);
        g.fill(rightX, py + 78, px + panelW - 12, py + panelH - 12, panel);
        g.outline(rightX, py + 78, rightW - 2, panelH - 90, border);

        g.text(font, "V", px + 20, py + 18, accent, true);
        g.text(font, "VANTA", px + 38, py + 18, 0xFFF4EEFF, true);
        g.text(font, "MODULES", px + 150, py + 20, accent, true);
        g.text(font, "HUD", px + 220, py + 20, 0xFF9588A3, false);
        g.text(font, "PROFILES", px + 264, py + 20, 0xFF9588A3, false);
        g.text(font, "SETTINGS", px + 338, py + 20, 0xFF9588A3, false);
        g.text(font, "Minecraft 26.2  •  Right Shift", px + panelW - 190, py + 20, 0xFFB6A7C2, false);

        g.text(font, label(selectedCategory), contentX, py + 76, 0xFFF1EAFF, true);
        g.text(font, categoryDescription(selectedCategory), contentX, py + 89, 0xFF9D8BAA, false);

        Module selected = selectedModule();
        if (selected != null) {
            int sx = rightX + 14, sy = py + 96;
            g.text(font, selected.name, sx, sy, 0xFFF5EDFF, true);
            g.text(font, selected.enabled ? "● ENABLED" : "○ DISABLED", sx, sy + 18,
                    selected.enabled ? accent : 0xFF776B82, true);
            drawWrapped(g, selected.description, sx, sy + 42, rightW - 28, 0xFFB6A8C1);
            String widget = widgetId(selected.name);
            if (widget != null) {
                g.text(font, "HUD APPEARANCE", sx, py + 218, accent, true);
                g.text(font, "Scale  " + Math.round(HudLayout.scale(widget) * 100f) + "%", sx, py + 348, 0xFFD0C2DB, false);
                g.text(font, "Style  " + HudLayout.style(widget).label, sx, py + 361, 0xFFD0C2DB, false);
                g.text(font, "Background  " + (HudLayout.background(widget) ? "On" : "Off"), sx, py + 374, 0xFFD0C2DB, false);
                drawPreview(g, widget, sx, py + 398, rightW - 28, 52);
            } else if (hasScalarSetting(selected.name)) {
                g.text(font, "VALUE", sx, py + 218, accent, true);
                g.centeredText(font, settingValue(selected.name), rightX + rightW / 2, py + 252, 0xFFF4EAFF);
            }
        }

        g.text(font, "Right Ctrl = HUD editor   •   Left Alt = Freelook   •   C = Zoom", px + 20, py + panelH - 21, 0xFF8F7C9E, false);
    }

    private void drawPreview(GuiGraphicsExtractor g, String widget, int x, int y, int w, int h) {
        HudLayout.Style style = HudLayout.style(widget);
        if (HudLayout.background(widget)) {
            int c = switch (style) {
                case VANILLA -> 0x66000000;
                case CLEAN -> 0xDD13151B;
                case GLASS -> 0x8814121A;
                case VANTA -> 0xDD11091D;
            };
            g.fill(x, y, x + w, y + h, c);
            if (style != HudLayout.Style.VANILLA) g.outline(x, y, w, h, style == HudLayout.Style.VANTA ? 0xAA9A46EC : 0x665B5266);
        }
        int ac = style == HudLayout.Style.VANTA ? 0xFFB15CFF : 0xFFE2DCE8;
        g.text(font, "Preview", x + 8, y + 7, ac, true);
        g.text(font, "Scale " + Math.round(HudLayout.scale(widget) * 100f) + "%", x + 8, y + 24, 0xFFD4C8DC, false);
    }

    private Module selectedModule() {
        Module selected = VantaClient.MODULES.get(selectedModuleName);
        if (selected != null && selected.category == selectedCategory) return selected;
        List<Module> list = VantaClient.MODULES.in(selectedCategory);
        return list.isEmpty() ? null : list.getFirst();
    }

    private String firstModuleName(Module.Category c) {
        List<Module> list = VantaClient.MODULES.in(c);
        return list.isEmpty() ? "" : list.getFirst().name;
    }

    private static String widgetId(String module) {
        return switch (module) {
            case "Saturation HUD" -> "saturation";
            case "Armor HUD" -> "armor";
            case "Totem Counter" -> "totems";
            case "Status Effect Bars" -> "effects";
            case "Keystrokes" -> "keys";
            case "FPS Counter", "Ping Display", "Coordinates", "Direction", "CPS Counter", "Memory Usage", "Clock", "Server Address", "Player Count" -> "stats";
            default -> null;
        };
    }

    private static boolean hasScalarSetting(String name) {
        return name.equals("Low Fire") || name.equals("Low Shield") || name.equals("Item Scaling")
                || name.equals("Zoom") || name.equals("Freelook");
    }

    private static String settingValue(String name) {
        return switch (name) {
            case "Low Fire" -> Math.round(VantaSettings.lowFireOffset * 100f) + "%";
            case "Low Shield" -> Math.round(VantaSettings.lowShieldOffset * 100f) + "%";
            case "Item Scaling" -> Math.round(VantaSettings.itemScale * 100f) + "%";
            case "Zoom" -> VantaSettings.zoomFov + " FOV";
            case "Freelook" -> String.format("%.2fx", VantaSettings.freelookSensitivity);
            default -> "";
        };
    }

    private static void adjustSetting(String name, int direction) {
        switch (name) {
            case "Low Fire" -> VantaSettings.lowFireOffset = clamp(VantaSettings.lowFireOffset + direction * 0.05f, 0f, 0.90f);
            case "Low Shield" -> VantaSettings.lowShieldOffset = clamp(VantaSettings.lowShieldOffset + direction * 0.05f, 0f, 0.80f);
            case "Item Scaling" -> VantaSettings.itemScale = clamp(VantaSettings.itemScale + direction * 0.05f, 0.50f, 1.25f);
            case "Zoom" -> VantaSettings.zoomFov = Math.max(10, Math.min(60, VantaSettings.zoomFov + direction * 5));
            case "Freelook" -> VantaSettings.freelookSensitivity = clamp(VantaSettings.freelookSensitivity + direction * 0.10f, 0.25f, 2.0f);
        }
    }

    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }

    private static String label(Module.Category category) {
        return switch (category) {
            case HUD -> "HUD";
            case PVP -> "PvP & Movement";
            case VISUAL -> "Render";
            case PERFORMANCE -> "Performance";
            case UTILITY -> "Utility";
        };
    }

    private static String categoryDescription(Module.Category category) {
        return switch (category) {
            case HUD -> "Resizable overlays and information widgets";
            case PVP -> "Legit movement and view helpers";
            case VISUAL -> "Client-side rendering controls";
            case PERFORMANCE -> "Reversible FPS and rendering options";
            case UTILITY -> "Editors and client configuration";
        };
    }

    private static void drawWrapped(GuiGraphicsExtractor g, String text, int x, int y, int maxWidth, int color) {
        String[] words = text.split(" ");
        String line = "";
        int yy = y;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (gWidth(candidate) > maxWidth && !line.isEmpty()) {
                g.text(MinecraftFontHolder.font(), line, x, yy, color, false);
                yy += 11;
                line = word;
            } else line = candidate;
        }
        if (!line.isEmpty()) g.text(MinecraftFontHolder.font(), line, x, yy, color, false);
    }

    private static int gWidth(String text) { return MinecraftFontHolder.font().width(text); }
    private static final class MinecraftFontHolder {
        static net.minecraft.client.gui.Font font() { return net.minecraft.client.Minecraft.getInstance().font; }
    }

    private static String guiStyleLabel() {
        return switch (VantaSettings.guiStyle.toUpperCase()) {
            case "VANILLA" -> "Vanilla";
            case "CLEAN" -> "Clean";
            case "GLASS" -> "Glass";
            default -> "Vanta";
        };
    }
    private static void cycleGuiStyle() {
        VantaSettings.guiStyle = switch (VantaSettings.guiStyle.toUpperCase()) {
            case "VANILLA" -> "CLEAN";
            case "CLEAN" -> "GLASS";
            case "GLASS" -> "VANTA";
            default -> "VANILLA";
        };
    }
    private static int guiShell() { return VantaSettings.guiStyle.equalsIgnoreCase("VANILLA") ? 0xF0161616 : VantaSettings.guiStyle.equalsIgnoreCase("CLEAN") ? 0xF00E1014 : 0xF0080710; }
    private static int guiPanel() { return VantaSettings.guiStyle.equalsIgnoreCase("VANILLA") ? 0xE0262626 : VantaSettings.guiStyle.equalsIgnoreCase("GLASS") ? 0x99100E17 : 0xE80D0A14; }
    private static int guiAccent() { return VantaSettings.guiStyle.equalsIgnoreCase("VANILLA") ? 0xFFFFFFFF : VantaSettings.guiStyle.equalsIgnoreCase("CLEAN") ? 0xFFD7DBE2 : 0xFFB15CFF; }
    private static int guiBorder() { return VantaSettings.guiStyle.equalsIgnoreCase("VANILLA") ? 0x665A5A5A : 0xAA6D28D9; }
    private static int guiHeaderTop() { return VantaSettings.guiStyle.equalsIgnoreCase("VANILLA") ? 0xF02A2A2A : 0xF0180A2B; }

    @Override public boolean isPauseScreen() { return false; }
}
