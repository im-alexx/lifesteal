package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageTracker.class)
public class DamageTrackerInvisKillCreditMixin {
    @Shadow
    @Final
    private LivingEntity entity;

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void lifesteal$hideInvisPlayerKillCredit(CallbackInfoReturnable<Text> cir) {
        if (!LifestealConfig.get().hideInvisPlayerKillCredit) {
            return;
        }

        if (!(entity.getPrimeAdversary() instanceof PlayerEntity attacker)) {
            return;
        }
        if (attacker == entity || !attacker.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return;
        }

        cir.setReturnValue(Text.literal(entity.getDisplayName().getString() + " was slain."));
    }
}
