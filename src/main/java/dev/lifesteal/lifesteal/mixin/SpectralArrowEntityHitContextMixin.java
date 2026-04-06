package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.ArrowEffectContext;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectralArrowEntity.class)
public class SpectralArrowEntityHitContextMixin {
    @Inject(method = "onHit", at = @At("HEAD"))
    private void lifesteal$enterArrowHitContext(CallbackInfo ci) {
        ArrowEffectContext.enterArrowHit();
    }

    @Inject(method = "onHit", at = @At("RETURN"))
    private void lifesteal$exitArrowHitContext(CallbackInfo ci) {
        ArrowEffectContext.exitArrowHit();
    }
}
