package dev.vanta.client.mixin;

import dev.vanta.client.VantaClient;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class NoPumpkinBlurMixin {
    @Redirect(
        method = "extractCameraOverlays",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"),
        require = 0
    )
    private ItemStack vanta$hidePumpkinOverlay(LocalPlayer player, EquipmentSlot slot) {
        if (VantaClient.MODULES.on("No Pumpkin Blur") && slot == EquipmentSlot.HEAD) return ItemStack.EMPTY;
        return player.getItemBySlot(slot);
    }
}
