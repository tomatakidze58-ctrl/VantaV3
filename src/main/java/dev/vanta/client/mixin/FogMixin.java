package dev.vanta.client.mixin;

import dev.vanta.client.VantaClient;
import dev.vanta.client.util.Reflect;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogMixin {
    @Inject(method = "setupFog", at = @At("RETURN"), require = 0)
    private void vanta$fogControls(Camera camera, int renderDistanceChunks, DeltaTracker delta, float partialTick,
                                   ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        FogData fog = cir.getReturnValue();
        if (fog == null) return;
        String medium = String.valueOf(Reflect.call(camera, "getFluidInCamera")).toUpperCase();
        boolean removeEnvironment =
                (medium.contains("LAVA") && VantaClient.MODULES.on("No Lava Fog")) ||
                (medium.contains("WATER") && VantaClient.MODULES.on("No Water Fog")) ||
                (medium.contains("POWDER") && VantaClient.MODULES.on("No Powder Snow Fog")) ||
                VantaClient.MODULES.on("No Atmospheric Fog") || effectFogWanted();

        float far = Math.max(256.0F, renderDistanceChunks * 16.0F);
        if (removeEnvironment) {
            Reflect.setField(fog, far, "environmentalStart");
            Reflect.setField(fog, far + 1.0F, "environmentalEnd");
            Reflect.setField(fog, far, "skyEnd");
            Reflect.setField(fog, far, "cloudEnd");
        }
        if (VantaClient.MODULES.on("No Render Distance Fog")) {
            Reflect.setField(fog, far - 1.0F, "renderDistanceStart");
            Reflect.setField(fog, far, "renderDistanceEnd");
        }
    }

    private static boolean effectFogWanted() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        boolean blindness = false, darkness = false;
        for (Object inst : mc.player.getActiveEffects()) {
            Object holder = Reflect.call(inst, "getEffect");
            String s = String.valueOf(holder).toLowerCase();
            blindness |= s.contains("blindness");
            darkness |= s.contains("darkness");
        }
        return (blindness && VantaClient.MODULES.on("No Blindness Fog")) ||
               (darkness && VantaClient.MODULES.on("No Darkness Fog"));
    }
}
