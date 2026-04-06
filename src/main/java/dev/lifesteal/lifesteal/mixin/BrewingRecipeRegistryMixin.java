package dev.lifesteal.lifesteal.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import dev.lifesteal.lifesteal.config.LifestealConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockBannedPotionRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (isBannedPotionRecipe(input, ingredient)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockBannedPotionCraft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (isBannedPotionRecipe(input, ingredient)) {
            cir.setReturnValue(input.copy());
        }
    }

    private static boolean isBannedPotionRecipe(ItemStack input, ItemStack ingredient) {
        return isBannedStrengthTwoRecipe(input, ingredient)
                || isBannedSwiftnessTwoRecipe(input, ingredient)
                || isBannedDebuffRecipe(input, ingredient)
                || isBannedInstantHealingRecipe(input, ingredient);
    }

    private static boolean isBannedStrengthTwoRecipe(ItemStack input, ItemStack ingredient) {
        if (LifestealConfig.get().allowStrengthII || !ingredient.isOf(Items.GLOWSTONE_DUST)) {
            return false;
        }
        return isAnyStrengthPotion(input);
    }

    private static boolean isBannedSwiftnessTwoRecipe(ItemStack input, ItemStack ingredient) {
        if (LifestealConfig.get().allowSwiftnessII || !ingredient.isOf(Items.GLOWSTONE_DUST)) {
            return false;
        }
        return isAnySwiftnessPotion(input);
    }

    private static boolean isBannedDebuffRecipe(ItemStack input, ItemStack ingredient) {
        if (LifestealConfig.get().allowDebuffPotions) {
            return false;
        }
        return (ingredient.isOf(Items.SPIDER_EYE) && isAwkwardPotion(input))
                || (isAnyPoisonPotion(input) && (
                ingredient.isOf(Items.REDSTONE)
                        || ingredient.isOf(Items.GLOWSTONE_DUST)
                        || ingredient.isOf(Items.GUNPOWDER)
                        || ingredient.isOf(Items.DRAGON_BREATH)
        ))
                || (ingredient.isOf(Items.FERMENTED_SPIDER_EYE) && (
                isWaterPotion(input)
                        || isAnyStrengthPotion(input)
                        || isAnyPoisonPotion(input)
                        || isAnyInstantHealingPotion(input)
        ))
                || (isAnyInstantDamagePotion(input) && (
                ingredient.isOf(Items.GLOWSTONE_DUST)
                        || ingredient.isOf(Items.GUNPOWDER)
                        || ingredient.isOf(Items.DRAGON_BREATH)
        ))
                || (isAnyWeaknessPotion(input) && (
                ingredient.isOf(Items.REDSTONE)
                        || ingredient.isOf(Items.GUNPOWDER)
                        || ingredient.isOf(Items.DRAGON_BREATH)
        ));
    }

    private static boolean isBannedInstantHealingRecipe(ItemStack input, ItemStack ingredient) {
        if (LifestealConfig.get().allowInstantHealingPotions) {
            return false;
        }
        return (ingredient.isOf(Items.GLISTERING_MELON_SLICE) && isAwkwardPotion(input))
                || (isAnyInstantHealingPotion(input) && (
                ingredient.isOf(Items.GLOWSTONE_DUST)
                        || ingredient.isOf(Items.GUNPOWDER)
                        || ingredient.isOf(Items.DRAGON_BREATH)
        ));
    }

    private static boolean isAwkwardPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.AWKWARD);
    }

    private static boolean isWaterPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.WATER);
    }

    private static boolean isAnyStrengthPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.STRENGTH)
                || matchesPotion(stack, Potions.LONG_STRENGTH)
                || matchesPotion(stack, Potions.STRONG_STRENGTH);
    }

    private static boolean isAnySwiftnessPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.SWIFTNESS)
                || matchesPotion(stack, Potions.LONG_SWIFTNESS)
                || matchesPotion(stack, Potions.STRONG_SWIFTNESS);
    }

    private static boolean isAnyPoisonPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.POISON)
                || matchesPotion(stack, Potions.LONG_POISON)
                || matchesPotion(stack, Potions.STRONG_POISON);
    }

    private static boolean isAnyInstantDamagePotion(ItemStack stack) {
        return matchesPotion(stack, Potions.HARMING)
                || matchesPotion(stack, Potions.STRONG_HARMING);
    }

    private static boolean isAnyInstantHealingPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.HEALING)
                || matchesPotion(stack, Potions.STRONG_HEALING);
    }

    private static boolean isAnyWeaknessPotion(ItemStack stack) {
        return matchesPotion(stack, Potions.WEAKNESS)
                || matchesPotion(stack, Potions.LONG_WEAKNESS);
    }

    private static boolean matchesPotion(
            ItemStack stack,
            net.minecraft.registry.entry.RegistryEntry<net.minecraft.potion.Potion> candidate
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        PotionContentsComponent contents = stack.getOrDefault(
                DataComponentTypes.POTION_CONTENTS,
                PotionContentsComponent.DEFAULT
        );

        return contents.potion()
                .filter(potion -> potion.equals(candidate))
                .isPresent();
    }
}
