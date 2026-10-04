package dev.vanta.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import dev.vanta.client.camera.FreelookState;
import dev.vanta.client.gui.HudEditorScreen;
import dev.vanta.client.gui.VantaScreen;
import dev.vanta.client.hud.VantaHud;
import dev.vanta.client.module.ModuleManager;
import dev.vanta.client.util.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;
import java.util.Deque;

public final class VantaClient implements ClientModInitializer {
    public static final String MOD_ID = "vantaclient";
    public static final ModuleManager MODULES = new ModuleManager();
    private static final Deque<Long> CLICKS = new ArrayDeque<>();
    private KeyMapping menuKey, hudEditorKey, zoomKey, freelookKey;

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    @Override
    public void onInitializeClient() {
        Config.load(MODULES);
        KeyMapping.Category cat = KeyMapping.Category.register(id("main"));
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.vantaclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        hudEditorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.vantaclient.hud_editor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_CONTROL, cat));
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.vantaclient.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));
        freelookKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.vantaclient.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, cat));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) client.gui.setScreen(new VantaScreen());
            while (hudEditorKey.consumeClick()) client.gui.setScreen(new HudEditorScreen());
            MODULES.tick(client);
            VantaHud.setZoomHeld(zoomKey.isDown());
            FreelookState.updateHeld(freelookKey.isDown());
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("hud"), VantaHud::render);
    }

    public static void registerClick() {
        long now = System.currentTimeMillis();
        CLICKS.addLast(now);
        while (!CLICKS.isEmpty() && CLICKS.peekFirst() < now - 1000L) CLICKS.removeFirst();
    }

    public static int cps() {
        long now = System.currentTimeMillis();
        while (!CLICKS.isEmpty() && CLICKS.peekFirst() < now - 1000L) CLICKS.removeFirst();
        return CLICKS.size();
    }
}
