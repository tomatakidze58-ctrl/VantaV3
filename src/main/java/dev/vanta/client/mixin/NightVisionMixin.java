package dev.vanta.client.mixin;

import dev.vanta.client.VantaClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps a real night-vision effect visually steady when Vanta Night Vision is enabled. */
@Mixin(GameRenderer.class)
public abstract class NightVisionMixin {
    @Inject(method = "nightVisionScale", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vanta$steadyNightVision(CallbackInfoReturnable<Float> cir) {
        if (VantaClient.MODULES.on("Night Vision")) cir.setReturnValue(1.0F);
    }
}
