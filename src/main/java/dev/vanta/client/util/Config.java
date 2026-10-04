package dev.vanta.client.util;

import dev.vanta.client.hud.HudLayout;
import dev.vanta.client.module.Module;
import dev.vanta.client.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("vantaclient.properties");
    private Config() {}

    public static void load(ModuleManager modules) {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            for (Module m : modules.all()) {
                String v = p.getProperty("module." + m.name);
                if (v != null) m.enabled = Boolean.parseBoolean(v);
            }
            for (String id : HudLayout.all().keySet()) {
                HudLayout.Pos fallback = HudLayout.get(id);
                int x = parseInt(p.getProperty("hud." + id + ".x"), fallback.x());
                int y = parseInt(p.getProperty("hud." + id + ".y"), fallback.y());
                HudLayout.put(id, x, y);

                HudLayout.Appearance a = HudLayout.appearance(id);
                HudLayout.setScale(id, parseFloat(p.getProperty("hud." + id + ".scale"), a.scale()));
                HudLayout.setBackground(id, Boolean.parseBoolean(p.getProperty("hud." + id + ".background", Boolean.toString(a.background()))));
                HudLayout.setStyle(id, HudLayout.Style.parse(p.getProperty("hud." + id + ".style"), a.style()));
            }
            VantaSettings.lowFireOffset = parseFloat(p.getProperty("setting.lowFireOffset"), VantaSettings.lowFireOffset);
            VantaSettings.lowShieldOffset = parseFloat(p.getProperty("setting.lowShieldOffset"), VantaSettings.lowShieldOffset);
            VantaSettings.itemScale = parseFloat(p.getProperty("setting.itemScale"), VantaSettings.itemScale);
            VantaSettings.zoomFov = parseInt(p.getProperty("setting.zoomFov"), VantaSettings.zoomFov);
            VantaSettings.smoothHud = Boolean.parseBoolean(p.getProperty("setting.smoothHud", Boolean.toString(VantaSettings.smoothHud)));
            VantaSettings.transparentModuleCards = Boolean.parseBoolean(p.getProperty("setting.transparentModuleCards", Boolean.toString(VantaSettings.transparentModuleCards)));
            VantaSettings.guiStyle = p.getProperty("setting.guiStyle", VantaSettings.guiStyle);
            VantaSettings.freelookSensitivity = parseFloat(p.getProperty("setting.freelookSensitivity"), VantaSettings.freelookSensitivity);
        } catch (Exception ignored) {}
    }

    public static void save(ModuleManager modules) {
        Properties p = new Properties();
        for (Module m : modules.all()) p.setProperty("module." + m.name, Boolean.toString(m.enabled));
        for (var e : HudLayout.all().entrySet()) {
            String id = e.getKey();
            p.setProperty("hud." + id + ".x", Integer.toString(e.getValue().x()));
            p.setProperty("hud." + id + ".y", Integer.toString(e.getValue().y()));
            HudLayout.Appearance a = HudLayout.appearance(id);
            p.setProperty("hud." + id + ".scale", Float.toString(a.scale()));
            p.setProperty("hud." + id + ".background", Boolean.toString(a.background()));
            p.setProperty("hud." + id + ".style", a.style().name());
        }
        p.setProperty("setting.lowFireOffset", Float.toString(VantaSettings.lowFireOffset));
        p.setProperty("setting.lowShieldOffset", Float.toString(VantaSettings.lowShieldOffset));
        p.setProperty("setting.itemScale", Float.toString(VantaSettings.itemScale));
        p.setProperty("setting.zoomFov", Integer.toString(VantaSettings.zoomFov));
        p.setProperty("setting.smoothHud", Boolean.toString(VantaSettings.smoothHud));
        p.setProperty("setting.transparentModuleCards", Boolean.toString(VantaSettings.transparentModuleCards));
        p.setProperty("setting.guiStyle", VantaSettings.guiStyle);
        p.setProperty("setting.freelookSensitivity", Float.toString(VantaSettings.freelookSensitivity));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "Vanta Client V3"); }
        } catch (Exception ignored) {}
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static float parseFloat(String value, float fallback) {
        try { return value == null ? fallback : Float.parseFloat(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
