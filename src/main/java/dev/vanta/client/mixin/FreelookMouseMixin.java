package dev.vanta.client.mixin;

import dev.vanta.client.camera.FreelookState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures the normal mouse turn, feeds it into the freelook camera, then restores player rotation. */
@Mixin(MouseHandler.class)
public abstract class FreelookMouseMixin {
    @Unique private float vanta$oldYaw;
    @Unique private float vanta$oldPitch;
    @Unique private boolean vanta$capturing;

    @Inject(method = "turnPlayer", at = @At("HEAD"), require = 0)
    private void vanta$beforeTurn(double movementTime, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        vanta$capturing = FreelookState.active() && mc.player != null;
        if (vanta$capturing) {
            vanta$oldYaw = mc.player.getYRot();
            vanta$oldPitch = mc.player.getXRot();
        }
    }

    @Inject(method = "turnPlayer", at = @At("TAIL"), require = 0)
    private void vanta$afterTurn(double movementTime, CallbackInfo ci) {
        if (!vanta$capturing) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float yawDelta = mc.player.getYRot() - vanta$oldYaw;
        float pitchDelta = mc.player.getXRot() - vanta$oldPitch;
        FreelookState.addTurn(yawDelta, pitchDelta);
        mc.player.setYRot(vanta$oldYaw);
        mc.player.setXRot(vanta$oldPitch);
        vanta$capturing = false;
    }
}
