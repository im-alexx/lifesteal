package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMaceBlockMixin {
    @Shadow
    @Final
    private ItemStack result;

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockMaceRecipeMatch(CraftingRecipeInput input, World world, CallbackInfoReturnable<Boolean> cir) {
        if (isMaceRecipeBlocked()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockMaceRecipeCraft(
            CraftingRecipeInput input,
            RegistryWrapper.WrapperLookup registries,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (isMaceRecipeBlocked()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    private boolean isMaceRecipeBlocked() {
        return LifestealConfig.get().lockMaceRecipe && result.isOf(Items.MACE);
    }
}
