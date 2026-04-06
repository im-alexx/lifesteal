package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.InstantHealthOrDamageStatusEffect")
public abstract class InstantHealthOrDamageStatusEffectMixin {
    @Inject(method = "applyInstantEffect", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockDisabledInstantEffect(
            net.minecraft.server.world.ServerWorld world,
            net.minecraft.entity.Entity source,
            net.minecraft.entity.Entity attacker,
            LivingEntity target,
            int amplifier,
            double proximity,
            CallbackInfo ci
    ) {
        if (isBlockedInstantEffect(target)) {
            ci.cancel();
        }
    }

    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void lifesteal$blockDisabledInstantTickEffect(
            net.minecraft.server.world.ServerWorld world,
            LivingEntity entity,
            int amplifier,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (isBlockedInstantEffect(entity)) {
            cir.setReturnValue(false);
        }
    }

    private boolean isBlockedInstantEffect(LivingEntity target) {
        StatusEffect self = (StatusEffect) (Object) this;
        if (self == StatusEffects.INSTANT_HEALTH.value() && !LifestealConfig.get().allowInstantHealingPotions) {
            return true;
        }
        return self == StatusEffects.INSTANT_DAMAGE.value()
                && target instanceof net.minecraft.entity.player.PlayerEntity
                && !LifestealConfig.get().allowDebuffPotions;
    }
}
