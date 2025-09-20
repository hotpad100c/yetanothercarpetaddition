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

package mypals.ml.mixin.features.chokolatekiller;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnderDragonPart.class)
public abstract class EnderDragonPartMixin extends Entity {

    /* @Shadow
     @Final
     public EnderDragonEntity owner;
 */
    public EnderDragonPartMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /*@Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        EnderDragonPart self = (EnderDragonPart) (Object) this;
        if (this.owner.head == self) {
            if (this.owner.isAlive() && this.owner.getHealth() > 0) {
                return this.owner.interact(player, hand);
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }*/
}
