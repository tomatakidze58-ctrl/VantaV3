package dev.vanta.client.mixin;

import dev.vanta.client.VantaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class BoatMapMixin {
    @Shadow private Minecraft minecraft;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;
    @Shadow private float mainHandHeight;
    @Shadow private float offHandHeight;

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void vanta$keepMapsVisibleInBoats(CallbackInfo ci) {
        if (!VantaClient.MODULES.on("Boat Map Visibility") || minecraft.player == null) return;
        Object vehicle = minecraft.player.getVehicle();
        if (vehicle == null || !vehicle.getClass().getSimpleName().toLowerCase().contains("boat")) return;
        if (mainHandItem != null && mainHandItem.getItem() == Items.FILLED_MAP) mainHandHeight = Math.max(mainHandHeight, 0.92F);
        if (offHandItem != null && offHandItem.getItem() == Items.FILLED_MAP) offHandHeight = Math.max(offHandHeight, 0.92F);
    }
}
