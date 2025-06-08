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

package mypals.ml.mixin.features.bouncierSlime;

import mypals.ml.interfaces.BlockBehaviorExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.Portal;
import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract void tryUsePortal(Portal portal, BlockPos pos);

    @Shadow
    public abstract World getWorld();

    @Shadow
    private World world;

    @Inject(method = "checkBlockCollision", at = @At("HEAD"), cancellable = true)
    protected void checkBlockCollision(CallbackInfo ci) {
        checkSurfaceCollision((Entity) (Object) this);
    }

    @Unique
    private void checkSurfaceCollision(Entity entity) {
        if (!entity.isAlive()) {
            return;
        }

        World world = entity.getWorld();
        Box entityBox = entity.getBoundingBox();
        BlockPos minPos = BlockPos.ofFloored(entityBox.minX - 1, entityBox.minY - 1, entityBox.minZ - 1);
        BlockPos maxPos = BlockPos.ofFloored(entityBox.maxX + 1, entityBox.maxY + 1, entityBox.maxZ + 1);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    mutable.set(x, y, z);
                    if (!world.isRegionLoaded(minPos, maxPos)) {
                        continue;
                    }

                    BlockState blockState = world.getBlockState(mutable);
                    VoxelShape blockShape = blockState.getCollisionShape(world, mutable);

                    try {
                        ((BlockBehaviorExtension) blockState.getBlock()).yaca$onEntityTouch(world, mutable, entity);
                    } catch (Throwable throwable) {
                        CrashReport crashReport = CrashReport.create(throwable, "Colliding entity with block");
                        CrashReportSection crashReportSection = crashReport.addElement("Block being collided with");
                        CrashReportSection.addBlockInfo(crashReportSection, this.world, mutable, blockState);
                        throw new CrashException(crashReport);
                    }
                    /*if (!blockShape.isEmpty() && VoxelShapes.matchesAnywhere(
                            VoxelShapes.cuboid(entityBox),
                            blockShape.offset(x, y, z),
                            BooleanBiFunction.AND)) {
                        if (!entityBox.contains(mutable.getX() + 0.5, mutable.getY() + 0.5, mutable.getZ() + 0.5)) {

                        }
                    }*/
                }
            }
        }
    }
}
