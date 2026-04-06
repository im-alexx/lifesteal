package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.Lifesteal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerEnderChestHeartBlockMixin {
    @Shadow public abstract ItemStack getCursorStack();
    @Shadow public abstract Slot getSlot(int index);

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockHeartsInEnderChest(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotIndex < 0) {
            return;
        }

        Slot targetSlot;
        try {
            targetSlot = this.getSlot(slotIndex);
        } catch (IndexOutOfBoundsException ignored) {
            return;
        }
        if (!(targetSlot.inventory instanceof EnderChestInventory)) {
            return;
        }

        ItemStack cursor = this.getCursorStack();
        if (cursor.isOf(Lifesteal.HEART)) {
            ci.cancel();
            return;
        }

        if (actionType == SlotActionType.SWAP && button >= 0 && button < 9) {
            ItemStack hotbarStack = player.getInventory().getStack(button);
            if (hotbarStack.isOf(Lifesteal.HEART)) {
                ci.cancel();
            }
        }
    }
}
