package mypals.ml.mixin.features.moreCollidableEntities;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({WardenEntity.class, EnderDragonPart.class, GhastEntity.class, FallingBlockEntity.class,
        MinecartEntity.class, StriderEntity.class, TntEntity.class, IronGolemEntity.class,
        SnifferEntity.class, EvokerFangsEntity.class, CamelEntity.class, PlayerEntity.class,
        HoglinEntity.class, HorseEntity.class, SkeletonHorseEntity.class, ZombieHorseEntity.class,
        TridentEntity.class, DonkeyEntity.class, LlamaEntity.class, ZoglinEntity.class})
public abstract class EntityCollisionMixin extends Entity {
    public EntityCollisionMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return YetAnotherCarpetAdditionRules.moreHardCollisions && BoatEntity.canCollide(this, other);
    }

    @Override
    public boolean isPushable() {
        return YetAnotherCarpetAdditionRules.moreHardCollisions || super.isPushable();
    }

    @Override
    public boolean isCollidable() {
        return YetAnotherCarpetAdditionRules.moreHardCollisions;
    }
}
