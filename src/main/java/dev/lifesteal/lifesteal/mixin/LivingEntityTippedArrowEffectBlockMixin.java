package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.ArrowEffectContext;
import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityTippedArrowEffectBlockMixin {
    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lifesteal$blockTippedArrowEffectsOnPlayers(
            StatusEffectInstance effect,
            Entity source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!shouldBlockArrowEffect(effect)) {
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lifesteal$blockTippedArrowEffectsOnPlayers(
            StatusEffectInstance effect,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!shouldBlockArrowEffect(effect)) {
            return;
        }
        cir.setReturnValue(false);
    }

    private boolean shouldBlockArrowEffect(StatusEffectInstance effect) {
        if (effect == null || LifestealConfig.get().allowTippedArrows) {
            return false;
        }
        if (!((Object) this instanceof PlayerEntity)) {
            return false;
        }
        return ArrowEffectContext.isInsideArrowHit();
    }
}
