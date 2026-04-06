package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityRiptideCooldownMixin {
    @Inject(method = "useRiptide(IFLnet/minecraft/item/ItemStack;)V", at = @At("TAIL"), require = 0)
    private void lifesteal$applyRiptideCooldown(int riptideLevel, float speed, ItemStack stack, CallbackInfo ci) {
        if (!LifestealConfig.get().enableRiptideCooldown) {
            return;
        }
        if (!hasRiptideEnchant(stack)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) (Object) this;
        int cooldownTicks = LifestealConfig.get().riptideCooldown;
        if (cooldownTicks <= 0 || player.getItemCooldownManager().isCoolingDown(stack)) {
            return;
        }
        player.getItemCooldownManager().set(stack, cooldownTicks);
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
