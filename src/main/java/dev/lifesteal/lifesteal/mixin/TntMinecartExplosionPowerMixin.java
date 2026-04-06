package dev.lifesteal.lifesteal.mixin;

import dev.lifesteal.lifesteal.config.LifestealConfig;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntMinecartEntity.class)
public class TntMinecartExplosionPowerMixin {
    @Inject(
            method = "explode(Lnet/minecraft/entity/damage/DamageSource;D)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lifesteal$blockTntMinecartExplosionsIfDisabled(CallbackInfo ci) {
        if (LifestealConfig.get().allowTNTMinecarts) {
            return;
        }
        TntMinecartEntity minecart = (TntMinecartEntity) (Object) this;
        if (minecart.getEntityWorld() instanceof ServerWorld world) {
            double x = minecart.getX();
            double y = minecart.getY();
            double z = minecart.getZ();
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            world.spawnParticles(ParticleTypes.EXPLOSION, x, y, z, 16, 0.4D, 0.4D, 0.4D, 0.02D);
            world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, 1.0F);
        }
        minecart.discard();
        ci.cancel();
    }

    @ModifyArg(
            method = "explode(Lnet/minecraft/entity/damage/DamageSource;D)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)V"
            ),
            index = 6
    )
    private float lifesteal$halveTntMinecartExplosionRadius(float power) {
        if (!LifestealConfig.get().nerfTntMinecarts) {
            return power;
        }
        return power * 0.5F;
    }
}
