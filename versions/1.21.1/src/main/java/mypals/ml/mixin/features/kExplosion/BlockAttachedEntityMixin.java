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

package mypals.ml.mixin.features.kExplosion;

import mypals.ml.interfaces.ExplosionExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

//@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = "<1.21.2"))
@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin extends Entity{
    public BlockAttachedEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Unique
    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return ((ExplosionExtension)explosion).preservesDecorativeEntities() ? super.ignoreExplosion(explosion) : true;
    }
}
