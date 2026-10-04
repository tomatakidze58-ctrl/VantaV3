package dev.vanta.client.gui;

import dev.vanta.client.VantaClient;
import dev.vanta.client.hud.HudLayout;
import dev.vanta.client.util.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Drag widgets and tune size/style/background without leaving Minecraft. */
public final class HudEditorScreen extends Screen {
    private String selected;
    private String dragging;
    private double grabX, grabY;

    public HudEditorScreen() { this("saturation"); }
    public HudEditorScreen(String selected) {
        super(Component.literal("Vanta HUD Editor"));
        this.selected = selected == null ? "saturation" : selected;
    }

    @Override
    protected void init() {
        int right = width - 174;
        this.addRenderableWidget(Button.builder(Component.literal("Size -"), b -> {
            HudLayout.setScale(selected, HudLayout.scale(selected) - 0.05f);
            Config.save(VantaClient.MODULES);
        }).bounds(right, 82, 74, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Size +"), b -> {
            HudLayout.setScale(selected, HudLayout.scale(selected) + 0.05f);
            Config.save(VantaClient.MODULES);
        }).bounds(right + 78, 82, 74, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Style"), b -> {
            HudLayout.cycleStyle(selected);
            Config.save(VantaClient.MODULES);
        }).bounds(right, 110, 74, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Background"), b -> {
            HudLayout.setBackground(selected, !HudLayout.background(selected));
            Config.save(VantaClient.MODULES);
        }).bounds(right + 78, 110, 74, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Back to Vanta"), b -> this.minecraft.gui.setScreen(new VantaScreen()))
                .bounds(right, height - 34, 152, 22).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.fill(0, 0, width, height, 0xB0080710);
        g.fillGradient(0, 0, width, 56, 0xE51C0B30, 0xB0080710);
        g.centeredText(font, "VANTA HUD EDITOR", width / 2, 14, 0xFFF5EDFF);
        g.centeredText(font, "Drag a widget • click it to edit • size 55%-200%", width / 2, 31, 0xFFAF9ABE);

        for (String id : HudLayout.all().keySet()) {
            HudLayout.Pos p = HudLayout.get(id);
            HudLayout.Size base = HudLayout.size(id);
            float scale = HudLayout.scale(id);
            int w = Math.max(20, Math.round(base.width() * scale));
            int h = Math.max(16, Math.round(base.height() * scale));
            boolean active = id.equals(selected);
            boolean moving = id.equals(dragging);
            int bg = moving ? 0xAA6A27B5 : active ? 0x88521D8B : 0x55201829;
            int outline = moving ? 0xFFE0B4FF : active ? 0xFFB15CFF : 0x886A5775;
            g.fill(p.x(), p.y(), p.x() + w, p.y() + h, bg);
            g.outline(p.x(), p.y(), w, h, outline);
            g.text(font, label(id), p.x() + 5, p.y() + 5, 0xFFF4EAFF, true);
            g.text(font, Math.round(scale * 100f) + "%", p.x() + 5, p.y() + 17, 0xFFBBA9C8, false);
        }

        int right = width - 174;
        g.fill(right - 8, 54, width - 8, 170, 0xCC0E0B15);
        g.outline(right - 8, 54, 166, 116, 0x88713BA4);
        g.text(font, label(selected), right, 61, 0xFFF2EAFF, true);
        g.text(font, "Scale: " + Math.round(HudLayout.scale(selected) * 100f) + "%", right, 142, 0xFFD2C2DF, false);
        g.text(font, "Style: " + HudLayout.style(selected).label, right, 153, 0xFFD2C2DF, false);
        g.text(font, "Background: " + (HudLayout.background(selected) ? "On" : "Off"), right, 164, 0xFFD2C2DF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (String id : HudLayout.all().keySet()) {
                HudLayout.Pos p = HudLayout.get(id);
                HudLayout.Size base = HudLayout.size(id);
                float scale = HudLayout.scale(id);
                int w = Math.round(base.width() * scale), h = Math.round(base.height() * scale);
                if (event.x() >= p.x() && event.x() <= p.x() + w && event.y() >= p.y() && event.y() <= p.y() + h) {
                    selected = id;
                    dragging = id;
                    grabX = event.x() - p.x();
                    grabY = event.y() - p.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            HudLayout.Size base = HudLayout.size(dragging);
            float scale = HudLayout.scale(dragging);
            int w = Math.round(base.width() * scale), h = Math.round(base.height() * scale);
            int nx = (int) Math.round(event.x() - grabX), ny = (int) Math.round(event.y() - grabY);
            nx = Math.max(0, Math.min(width - w, nx));
            ny = Math.max(48, Math.min(height - h, ny));
            HudLayout.put(dragging, nx, ny);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            Config.save(VantaClient.MODULES);
            return true;
        }
        return super.mouseReleased(event);
    }

    private static String label(String id) {
        return switch (id) {
            case "stats" -> "Info";
            case "saturation" -> "Saturation / Food";
            case "armor" -> "Armor HUD";
            case "totems" -> "Totem Counter";
            case "effects" -> "Status Effects";
            case "keys" -> "Keystrokes";
            default -> id;
        };
    }

    @Override public boolean isPauseScreen() { return false; }
}
