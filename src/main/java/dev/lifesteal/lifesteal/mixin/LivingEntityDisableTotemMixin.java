package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityDisableTotemMixin {
    @Inject(method = "tryUseDeathProtector", at = @At("HEAD"), cancellable = true)
    private void lifesteal$disableTotemDeathProtection(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!LifestealConfig.get().disableTotems) {
            return;
        }

        cir.setReturnValue(false);
    }
}
