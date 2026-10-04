package dev.vanta.client.module;

import dev.vanta.client.VantaClient;
import dev.vanta.client.util.Config;
import dev.vanta.client.util.Reflect;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private final List<Module> all = new ArrayList<>();
    private boolean lastAttackDown;

    private boolean fpsPresetApplied, bobApplied, shadowsApplied, fullbrightApplied, dynamicFpsApplied;
    private Object oldRenderDistance, oldSimulationDistance, oldEntityDistance, oldParticles;
    private Object oldBobbing, oldEntityShadows, oldGamma, oldMaxFps;

    public ModuleManager() {
        // HUD
        add("FPS Counter", Module.Category.HUD, "Current frames per second", true);
        add("Ping Display", Module.Category.HUD, "Server latency in milliseconds", true);
        add("Coordinates", Module.Category.HUD, "XYZ position", true);
        add("Direction", Module.Category.HUD, "Facing direction", false);
        add("Saturation HUD", Module.Category.HUD, "AppleSkin-style food and saturation pips", true);
        add("Armor HUD", Module.Category.HUD, "Minimal armor slots with durability", true);
        add("Totem Counter", Module.Category.HUD, "Totem icon and inventory count", true);
        add("Status Effect Bars", Module.Category.HUD, "Active potion effects with timers and bars", true);
        add("Smooth HUD", Module.Category.HUD, "Ease HUD widgets smoothly into their positions", true);
        add("Keystrokes", Module.Category.HUD, "WASD key state", true);
        add("CPS Counter", Module.Category.HUD, "Recent attack clicks", true);
        add("Memory Usage", Module.Category.HUD, "JVM memory use", false);
        add("Clock", Module.Category.HUD, "Local time", false);
        add("Server Address", Module.Category.HUD, "Current server", false);
        add("Player Count", Module.Category.HUD, "Players on server", false);

        // PvP / controls
        add("Auto Sprint", Module.Category.PVP, "Automatically sprint while moving forward", true);
        add("Crosshair", Module.Category.PVP, "Vanta purple crosshair", false);
        add("Zoom", Module.Category.PVP, "Hold C for smooth zoom", true);
        add("Freelook", Module.Category.PVP, "Hold Left Alt to look around without changing movement direction", true);

        // Visual / Bactro-style clean-room features
        add("Fullbright", Module.Category.VISUAL, "Uniform maximum lightmap brightness", true);
        add("Night Vision", Module.Category.VISUAL, "Client-side bright vision without a potion", false);
        add("No Pumpkin Blur", Module.Category.VISUAL, "Hide carved-pumpkin overlay", false);
        add("Low Fire", Module.Category.VISUAL, "Lower the first-person fire overlay", true);
        add("Low Shield", Module.Category.VISUAL, "Lower only the shield in first person", true);
        add("Item Scaling", Module.Category.VISUAL, "Scale first-person held items", false);
        add("Boat Map Visibility", Module.Category.VISUAL, "Keep map hand height stable in boats", false);
        add("Riptide Shield Fix", Module.Category.VISUAL, "Improve shield placement while riptiding", true);
        add("No Lava Fog", Module.Category.VISUAL, "Remove lava environmental fog", false);
        add("No Powder Snow Fog", Module.Category.VISUAL, "Remove powder-snow environmental fog", false);
        add("No Blindness Fog", Module.Category.VISUAL, "Reduce blindness fog", false);
        add("No Darkness Fog", Module.Category.VISUAL, "Reduce darkness fog", false);
        add("No Water Fog", Module.Category.VISUAL, "Remove underwater environmental fog", false);
        add("No Atmospheric Fog", Module.Category.VISUAL, "Remove atmospheric fog", false);
        add("No Render Distance Fog", Module.Category.VISUAL, "Push render-distance fog to the edge", false);
        add("View Bobbing Off", Module.Category.VISUAL, "Disable view bobbing", false);
        add("Entity Shadows Off", Module.Category.VISUAL, "Disable entity shadows", false);
        add("Particles Minimal", Module.Category.VISUAL, "Use minimal particles", false);

        // Performance
        add("FPS Boost", Module.Category.PERFORMANCE, "Reversible performance preset", true);
        add("Dynamic FPS", Module.Category.PERFORMANCE, "Lower FPS while unfocused", true);
        add("Render Distance Cap", Module.Category.PERFORMANCE, "Cap render distance at 10 chunks", false);
        add("Fast Particles", Module.Category.PERFORMANCE, "Use minimal particle rendering", true);
        add("Entity Distance", Module.Category.PERFORMANCE, "Reduce distant entity rendering", false);
        add("Chunk Performance", Module.Category.PERFORMANCE, "Cap simulation distance", false);

        // Utility
        add("HUD Editor", Module.Category.UTILITY, "Open with Right Ctrl to reposition HUD widgets", true);
    }

    private void add(String n, Module.Category c, String d, boolean on) { all.add(new Module(n, c, d, on)); }
    public List<Module> all() { return Collections.unmodifiableList(all); }
    public List<Module> in(Module.Category c) { return all.stream().filter(m -> m.category == c).toList(); }
    public Module get(String name) { return all.stream().filter(m -> m.name.equals(name)).findFirst().orElse(null); }
    public boolean on(String name) { Module m = get(name); return m != null && m.enabled; }

    public void toggle(Module m) { m.toggle(); Config.save(this); }

    public void tick(Minecraft mc) {
        if (mc == null) return;
        VantaSettings.smoothHud = on("Smooth HUD");
        if (mc.player != null && on("Auto Sprint") && Reflect.keyDown(mc.options, "keyUp")) {
            try { mc.player.setSprinting(true); } catch (Throwable ignored) {}
        }
        boolean attack = Reflect.keyDown(mc.options, "keyAttack");
        if (attack && !lastAttackDown) VantaClient.registerClick();
        lastAttackDown = attack;
        applyFullbrightOption(mc);
        applyStandaloneVisualOptions(mc);
        applyFpsPreset(mc);
        applyIndividualPerformance(mc);
        applyDynamicFps(mc);
    }

    private void applyFullbrightOption(Minecraft mc) {
        boolean wanted = on("Fullbright") || on("Night Vision");
        if (wanted && !fullbrightApplied) { oldGamma = Reflect.getOption(mc.options, "gamma"); fullbrightApplied = true; }
        if (wanted) Reflect.setOption(mc.options, 1.0D, "gamma");
        else if (fullbrightApplied) { restore(mc.options, oldGamma, "gamma"); oldGamma = null; fullbrightApplied = false; }
    }

    private void applyStandaloneVisualOptions(Minecraft mc) {
        boolean bobWanted = on("View Bobbing Off") || on("FPS Boost");
        if (bobWanted && !bobApplied) { oldBobbing = Reflect.getOption(mc.options, "bobView", "viewBobbing"); bobApplied = true; }
        if (bobWanted) Reflect.setOption(mc.options, false, "bobView", "viewBobbing");
        else if (bobApplied) { restore(mc.options, oldBobbing, "bobView", "viewBobbing"); oldBobbing = null; bobApplied = false; }

        boolean shadowWanted = on("Entity Shadows Off") || on("FPS Boost");
        if (shadowWanted && !shadowsApplied) { oldEntityShadows = Reflect.getOption(mc.options, "entityShadows"); shadowsApplied = true; }
        if (shadowWanted) Reflect.setOption(mc.options, false, "entityShadows");
        else if (shadowsApplied) { restore(mc.options, oldEntityShadows, "entityShadows"); oldEntityShadows = null; shadowsApplied = false; }
    }

    private void applyFpsPreset(Minecraft mc) {
        boolean wanted = on("FPS Boost");
        if (wanted && !fpsPresetApplied) {
            oldRenderDistance = Reflect.getOption(mc.options, "renderDistance");
            oldSimulationDistance = Reflect.getOption(mc.options, "simulationDistance");
            oldEntityDistance = Reflect.getOption(mc.options, "entityDistanceScaling", "entityDistance");
            oldParticles = particleValue(mc);
            fpsPresetApplied = true;
        }
        if (wanted) {
            capIntOption(mc.options, 10, "renderDistance");
            capIntOption(mc.options, 6, "simulationDistance");
            capDoubleOption(mc.options, 0.75D, "entityDistanceScaling", "entityDistance");
            setMinimalParticles(mc);
        } else if (fpsPresetApplied) {
            restore(mc.options, oldRenderDistance, "renderDistance");
            restore(mc.options, oldSimulationDistance, "simulationDistance");
            restore(mc.options, oldEntityDistance, "entityDistanceScaling", "entityDistance");
            restoreParticles(mc, oldParticles);
            oldRenderDistance = oldSimulationDistance = oldEntityDistance = oldParticles = null;
            fpsPresetApplied = false;
        }
    }

    private void applyIndividualPerformance(Minecraft mc) {
        if (on("Render Distance Cap") && !on("FPS Boost")) capIntOption(mc.options, 10, "renderDistance");
        if (on("Chunk Performance") && !on("FPS Boost")) capIntOption(mc.options, 6, "simulationDistance");
        if (on("Entity Distance") && !on("FPS Boost")) capDoubleOption(mc.options, 0.75D, "entityDistanceScaling", "entityDistance");
        if ((on("Fast Particles") || on("Particles Minimal")) && !on("FPS Boost")) setMinimalParticles(mc);
    }

    private void applyDynamicFps(Minecraft mc) {
        boolean wanted = on("Dynamic FPS");
        boolean active = windowActive(mc);
        if (wanted && !active) {
            if (!dynamicFpsApplied) { oldMaxFps = Reflect.getOption(mc.options, "framerateLimit", "maxFps"); dynamicFpsApplied = true; }
            capIntOption(mc.options, 30, "framerateLimit", "maxFps");
        } else if (dynamicFpsApplied) {
            restore(mc.options, oldMaxFps, "framerateLimit", "maxFps"); oldMaxFps = null; dynamicFpsApplied = false;
        }
    }

    private static boolean windowActive(Minecraft mc) {
        Object v = Reflect.call(mc, "isWindowActive");
        if (!(v instanceof Boolean)) v = Reflect.call(mc, "isWindowFocused");
        return !(v instanceof Boolean b) || b;
    }
    private static void capIntOption(Object options, int max, String... names) { Object c = Reflect.getOption(options, names); if (c instanceof Number n && n.intValue() > max) Reflect.setOption(options, max, names); }
    private static void capDoubleOption(Object options, double max, String... names) { Object c = Reflect.getOption(options, names); if (c instanceof Number n && n.doubleValue() > max) Reflect.setOption(options, max, names); }
    private static Object particleOption(Minecraft mc) { Object o = Reflect.call(mc.options, "particles"); return o != null ? o : Reflect.field(mc.options, "particles"); }
    private static Object particleValue(Minecraft mc) { Object o = particleOption(mc); return o == null ? null : Reflect.call(o, "get"); }
    private static void restoreParticles(Minecraft mc, Object value) { if (value != null) { Object o = particleOption(mc); if (o != null) Reflect.call(o, "set", value); } }
    private static void setMinimalParticles(Minecraft mc) {
        Object o = particleOption(mc), current = o == null ? null : Reflect.call(o, "get");
        if (current == null || !current.getClass().isEnum()) return;
        for (Object e : current.getClass().getEnumConstants()) if (((Enum<?>) e).name().equalsIgnoreCase("MINIMAL")) { Reflect.call(o, "set", e); return; }
    }
    private static void restore(Object options, Object value, String... names) { if (value != null) Reflect.setOption(options, value, names); }
}
