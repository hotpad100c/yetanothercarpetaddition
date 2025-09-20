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

package mypals.ml.mixin.features.updateLogger;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.features.updateAnylizer.UpdateLoggerHelper;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockMixin {
    @WrapMethod(method = "updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V")
    public void updateNeighbors(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, Operation<Void> original) {
        if (world.isClient() || !YetAnotherCarpetAdditionRules.updateCounter) {
            original.call(world, pos, flags, maxUpdateDepth);

        } else {
            if (UpdateLoggerHelper.isUpdating()) {
                original.call(world, pos, flags, maxUpdateDepth);
            } else {
                UpdateLoggerHelper.start();
                original.call(world, pos, flags, maxUpdateDepth);
                UpdateLoggerHelper.finish();
            }
        }

    }
}
