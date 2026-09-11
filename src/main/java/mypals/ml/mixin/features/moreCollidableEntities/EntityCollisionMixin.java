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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 260200
//$$ import net.minecraft.world.entity.EntityTypes;
//#endif

@Mixin({Warden.class, EnderDragonPart.class, Ghast.class, FallingBlockEntity.class,
        Minecart.class, Strider.class, PrimedTnt.class, IronGolem.class,
        Sniffer.class, EvokerFangs.class, Camel.class, Player.class,
        Hoglin.class, Horse.class, SkeletonHorse.class, ZombieHorse.class,
        ThrownTrident.class, Donkey.class, Llama.class, Zoglin.class})
public abstract class EntityCollisionMixin extends Entity {
    protected EntityCollisionMixin(EntityType<?> type, Level world) {
        super(type, world);
    }


    private boolean isMinecart() {
        //#if MC >= 260200
        //$$ return this.getType() == EntityTypes.MINECART ||
        //#else
        return this.getType() == EntityType.MINECART ||
        //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.CHEST_MINECART ||
                //#else
                this.getType() == EntityType.CHEST_MINECART ||
                //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.FURNACE_MINECART ||
                //#else
                this.getType() == EntityType.FURNACE_MINECART ||
                //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.HOPPER_MINECART ||
                //#else
                this.getType() == EntityType.HOPPER_MINECART ||
                //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.TNT_MINECART ||
                //#else
                this.getType() == EntityType.TNT_MINECART ||
                //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.COMMAND_BLOCK_MINECART ||
                //#else
                this.getType() == EntityType.COMMAND_BLOCK_MINECART ||
                //#endif
                //#if MC >= 260200
                //$$ this.getType() == EntityTypes.SPAWNER_MINECART;
                //#else
                this.getType() == EntityType.SPAWNER_MINECART;
                //#endif
    }

    //#if MC < 12103
    //$$ private boolean isBoat() {
    //$$     return this.getType() == EntityType.BOAT ||
    //$$             this.getType() == EntityType.CHEST_BOAT;
    //$$ }
    //$$
    //#else
     private boolean isBoat() {
             //#if MC >= 260200
             //$$ return this.getType() == EntityTypes.OAK_BOAT ||
             //#else
             return this.getType() == EntityType.OAK_BOAT ||
             //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.ACACIA_BOAT ||
                    //#else
                    this.getType() == EntityType.ACACIA_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.CHERRY_BOAT ||
                    //#else
                    this.getType() == EntityType.CHERRY_BOAT ||
                    //#endif
                   //#if MC >= 260200
                   //$$ this.getType() == EntityTypes.JUNGLE_BOAT ||
                   //#else
                   this.getType() == EntityType.JUNGLE_BOAT ||
                   //#endif
                   //#if MC >= 260200
                   //$$ this.getType() == EntityTypes.SPRUCE_BOAT ||
                   //#else
                   this.getType() == EntityType.SPRUCE_BOAT ||
                   //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.BIRCH_BOAT ||
                    //#else
                    this.getType() == EntityType.BIRCH_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.DARK_OAK_BOAT ||
                    //#else
                    this.getType() == EntityType.DARK_OAK_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.MANGROVE_BOAT ||
                    //#else
                    this.getType() == EntityType.MANGROVE_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.PALE_OAK_BOAT ||
                    //#else
                    this.getType() == EntityType.PALE_OAK_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.OAK_CHEST_BOAT ||
                    //#else
                    this.getType() == EntityType.OAK_CHEST_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.ACACIA_CHEST_BOAT ||
                    //#else
                    this.getType() == EntityType.ACACIA_CHEST_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.CHERRY_CHEST_BOAT ||
                    //#else
                    this.getType() == EntityType.CHERRY_CHEST_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.JUNGLE_CHEST_BOAT ||
                    //#else
                    this.getType() == EntityType.JUNGLE_CHEST_BOAT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.SPRUCE_CHEST_BOAT ||
                    //#else
                    this.getType() == EntityType.SPRUCE_CHEST_BOAT ||
                    //#endif
                   //#if MC >= 260200
                   //$$ this.getType() == EntityTypes.DARK_OAK_CHEST_BOAT ||
                   //#else
                   this.getType() == EntityType.DARK_OAK_CHEST_BOAT ||
                   //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.BAMBOO_RAFT ||
                    //#else
                    this.getType() == EntityType.BAMBOO_RAFT ||
                    //#endif
                    //#if MC >= 260200
                    //$$ this.getType() == EntityTypes.BAMBOO_CHEST_RAFT;
                    //#else
                    this.getType() == EntityType.BAMBOO_CHEST_RAFT;
                    //#endif
         }
    //#endif
    @Override
    public boolean canCollideWith(Entity other) {
        if (YetAnotherCarpetAdditionRules.moreHardCollisions && Boat.canVehicleCollide(this, other)) {
            return true;
        }

        if (other instanceof Boat || other instanceof Shulker) {
            return true;
        }

        if (isMinecart() || isBoat()) {
            return Boat.canVehicleCollide(this, other);
        }

        return super.canCollideWith(other);
    }

    @Override
    public boolean isPushable() {

        return YetAnotherCarpetAdditionRules.moreHardCollisions ||
                super.isPushable() ||
                isMinecart() ||
                isBoat();
    }

    @Override
    public boolean canBeCollidedWith(
            //#if MC>=12106
            Entity entity
            //#endif
    ) {
        return YetAnotherCarpetAdditionRules.moreHardCollisions || super.canBeCollidedWith(
                //#if MC>=12106
                entity
                //#endif
        ) || isBoat();
    }
}
