package dev.vanta.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vanta.client.VantaClient;
import dev.vanta.client.util.Reflect;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonItemMixin {
    @Inject(
        method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
        at = @At("HEAD"), require = 0
    )
    private void vanta$transformHeldItem(LivingEntity entity, ItemStack stack, ItemDisplayContext context,
                                         PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) return;
        if (VantaClient.MODULES.on("Item Scaling")) {
            float s = VantaSettings.itemScale;
            pose.scale(s, s, s);
        }
        if (stack.getItem() == Items.SHIELD && VantaClient.MODULES.on("Low Shield")) {
            pose.translate(0.0F, -VantaSettings.lowShieldOffset, 0.0F);
        }
        if (stack.getItem() == Items.SHIELD && VantaClient.MODULES.on("Riptide Shield Fix")
                && Reflect.boolCall(entity, "isAutoSpinAttack", false)) {
            pose.translate(0.12F, -0.08F, 0.0F);
        }
    }
}
