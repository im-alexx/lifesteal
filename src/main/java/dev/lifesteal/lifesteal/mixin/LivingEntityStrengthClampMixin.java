package dev.lifesteal.lifesteal.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import dev.lifesteal.lifesteal.config.LifestealConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityStrengthClampMixin {
    @ModifyVariable(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private StatusEffectInstance lifesteal$clampStrengthWithSource(StatusEffectInstance effect) {
        return clampPotionAmplifier(effect);
    }

    @ModifyVariable(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private StatusEffectInstance lifesteal$clampStrength(StatusEffectInstance effect) {
        return clampPotionAmplifier(effect);
    }

    private static StatusEffectInstance clampPotionAmplifier(StatusEffectInstance effect) {
        if (effect == null || effect.getAmplifier() <= 0) {
            return effect;
        }
        boolean clampStrength = effect.getEffectType() == StatusEffects.STRENGTH && !LifestealConfig.get().allowStrengthII;
        boolean clampSpeed = effect.getEffectType() == StatusEffects.SPEED && !LifestealConfig.get().allowSwiftnessII;
        if (!clampStrength && !clampSpeed) {
            return effect;
        }

        return new StatusEffectInstance(
                effect.getEffectType(),
                effect.getDuration(),
                0,
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon(),
                null
        );
    }
}
