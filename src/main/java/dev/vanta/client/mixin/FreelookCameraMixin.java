package dev.vanta.client.mixin;

import dev.vanta.client.camera.FreelookState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the camera-only yaw/pitch after vanilla aligns the camera with the player. */
@Mixin(Camera.class)
public abstract class FreelookCameraMixin {
    @Shadow protected abstract void setRotation(float yRot, float xRot, float roll);

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void vanta$freelookCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (FreelookState.active()) setRotation(FreelookState.yaw(), FreelookState.pitch(), 0.0F);
    }
}
