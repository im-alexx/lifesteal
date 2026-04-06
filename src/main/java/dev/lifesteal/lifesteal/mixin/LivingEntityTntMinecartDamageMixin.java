package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityTntMinecartDamageMixin {
    private static final float TNT_MINECART_MAX_DAMAGE = 18.0F;

    @ModifyVariable(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float lifesteal$capTntMinecartDamage(float amount, ServerWorld world, DamageSource source) {
        if (LifestealConfig.get().allowTNTMinecarts && !LifestealConfig.get().nerfTntMinecarts) {
            return amount;
        }
        if (source == null) {
            return amount;
        }
        if (source.getSource() instanceof TntMinecartEntity || source.getAttacker() instanceof TntMinecartEntity) {
            if (!LifestealConfig.get().allowTNTMinecarts) {
                return 0.0F;
            }
            return Math.min(amount, TNT_MINECART_MAX_DAMAGE);
        }
        return amount;
    }
}
