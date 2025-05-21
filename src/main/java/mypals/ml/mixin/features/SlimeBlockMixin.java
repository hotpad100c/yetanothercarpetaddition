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

package mypals.ml.mixin.features;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
//#if MC >= 12105
//$$ import net.minecraft.entity.EntityCollisionHandler;
//#endif

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin extends TranslucentBlock {
    public SlimeBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity
                                  //#if MC >= 12105
                                  //$$ , EntityCollisionHandler handler
                                  //#endif

    ) {
        if (!YetAnotherCarpetAdditionRules.bouncierSlime) return;
        if (!entity.bypassesLandingEffects()) bounceSide(entity);
    }

    @Unique
    private void bounceSide(Entity entity) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.y < 0.0 || vec3d.x < 0.0 || vec3d.z < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(-vec3d.x * d, -vec3d.y * d, -vec3d.z * d);
        }
    }
}
