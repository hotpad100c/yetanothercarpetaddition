/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.mixin.features.moreCollidableEntities;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType.*;
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
    protected EntityCollisionMixin(EntityType<?> type, World world) {
        super(type, world);
    }

   
    private boolean isMinecart() {
        return this.getType() == EntityType.MINECART ||
               this.getType() == EntityType.CHEST_MINECART ||
               this.getType() == EntityType.FURNACE_MINECART ||
               this.getType() == EntityType.HOPPER_MINECART ||
               this.getType() == EntityType.TNT_MINECART ||
               this.getType() == EntityType.COMMAND_BLOCK_MINECART ||
               this.getType() == EntityType.SPAWNER_MINECART;
    }
//#if MC <1213
    private boolean isBoat() {
        return this.getType() == EntityType.BOAT ||
               this.getType() == EntityType.CHEST_BOAT;
    }
//#else
//$  private boolean isBoat() {
//$          return this.getType() == EntityType.OAK_BOAT ||
//$                 this.getType() == EntityType.ACACIA_BOAT ||
//$                 this.getType() == EntityType.CHERRY_BOAT ||
//$                 this.getType() == EntityType.JUNGLE_BOAT ||
//$                 this.getType() == EntityType.SPRUCE_BOAT ||
//$                 this.getType() == EntityType.BIRCH_BOAT ||
//$                 this.getType() == EntityType.DARK_OAK_BOAT ||
//$                 this.getType() == EntityType.MANGROVE_BOAT ||
//$                 this.getType() == EntityType.PALE_OAK_BOAT ||
//$                 this.getType() == EntityType.OAK_CHEST_BOAT ||
//$                 this.getType() == EntityType.ACACIA_CHEST_BOAT ||
//$                 this.getType() == EntityType.CHERRY_CHEST_BOAT ||
//$                 this.getType() == EntityType.JUNGLE_CHEST_BOAT ||
//$                 this.getType() == EntityType.SPRUCE_CHEST_BOAT ||
//$                 this.getType() == EntityType.DARK_OAK_CHEST_BOAT ||
//$                 this.getType() == EntityType.BAMBOO_RAFT ||
//$                 this.getType() == EntityType.BAMBOO_CHEST_RAFT;
//$      }
//#endif
    @Override
    public boolean collidesWith(Entity other) {
        if (YetAnotherCarpetAdditionRules.moreHardCollisions && BoatEntity.canCollide(this, other)) {
            return true;
        }
        
        if (other instanceof BoatEntity || other instanceof ShulkerEntity) {
            return true;
        }
        
        if (isMinecart() || isBoat()) {
            return BoatEntity.canCollide(this, other);
        }
        
        return super.collidesWith(other);
    }

    @Override
    public boolean isPushable() {
        
        return YetAnotherCarpetAdditionRules.moreHardCollisions || 
               super.isPushable() || 
               isMinecart() || 
               isBoat();
    }

    @Override
    public boolean isCollidable() {
        return YetAnotherCarpetAdditionRules.moreHardCollisions || super.isCollidable() || isBoat();
    }
}
