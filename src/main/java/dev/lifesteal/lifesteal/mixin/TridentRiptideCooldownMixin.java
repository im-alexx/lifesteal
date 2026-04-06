package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public class TridentRiptideCooldownMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockRiptideUseDuringCooldown(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!LifestealConfig.get().enableRiptideCooldown) {
            return;
        }

        ItemStack stack = user.getStackInHand(hand);
        if (!hasRiptideEnchant(stack)) {
            return;
        }
        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    private static boolean hasRiptideEnchant(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
            if (enchantment.matchesKey(Enchantments.RIPTIDE) && enchantments.getLevel(enchantment) > 0) {
                return true;
            }
        }
        return false;
    }
}
