package dev.vanta.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vanta.client.VantaClient;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class LowFireMixin {
    @Inject(method = "submitFire", at = @At("HEAD"), require = 0)
    private static void vanta$lowFire(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (VantaClient.MODULES.on("Low Fire")) pose.translate(0.0F, VantaSettings.lowFireOffset, 0.0F);
    }
}
