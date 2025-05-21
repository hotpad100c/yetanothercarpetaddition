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

package mypals.ml.mixin.features.visualizers;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class PostProcessStateBlockMixin {
    @Inject(
            method = "postProcessState",
            at = @At(
                    //#if MC < 12102
                    target = "Lnet/minecraft/block/BlockState;getStateForNeighborUpdate(Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
                    //#else
                    //$$ target = "Lnet/minecraft/block/BlockState;getStateForNeighborUpdate(Lnet/minecraft/world/WorldView;Lnet/minecraft/world/tick/ScheduledTickView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/block/BlockState;",
                    //#endif
                    ordinal = 0, value = "INVOKE"
            )
    )
    private static void AddPPMarker(BlockState state, WorldAccess world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || world.isClient()) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) world, pos, BlockUpdateVisualizing.UpdateType.PP);

    }

}
