package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.Lifesteal;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotEnderChestHeartBlockMixin {
    @Shadow @Final public Inventory inventory;

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockHeartsInEnderChest(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (inventory instanceof EnderChestInventory && stack.isOf(Lifesteal.HEART)) {
            cir.setReturnValue(false);
        }
    }
}
