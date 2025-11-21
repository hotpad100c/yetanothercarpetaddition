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

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12102
//$$ import net.minecraft.util.math.random.Random;
//$$ import net.minecraft.world.WorldView;
//$$ import net.minecraft.world.tick.ScheduledTickView;
//#endif

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(
            method = "onPlace",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickTime(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "updateShape",
            at = @At("HEAD")
    )
    private void AddPPMarker(BlockState state,
                             //#if MC >= 12102
                             //$$ WorldView world, ScheduledTickView tickView,
                             //#else
                             Direction direction, BlockState neighborState, LevelAccessor world,
                             //#endif
                             BlockPos pos,
                             //#if MC >= 12102
                             //$$ Direction direction,BlockPos neighborPos,BlockState neighborState, Random random,
                             //#else
                             BlockPos neighborPos,
                             //#endif
                             CallbackInfoReturnable<BlockState> cir) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || world.isClientSide()) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerLevel) (Object) world, pos.immutable(), BlockUpdateVisualizing.UpdateType.PP);

    }

    @Inject(
            method = "updateIndirectNeighbourShapes",
            at = @At("HEAD"),
            cancellable = true
    )
    public void prepare(BlockState state, LevelAccessor world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "defaultDestroyTime",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getHardness(CallbackInfoReturnable<Float> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoHardness) {
            cir.setReturnValue(0f);
        }
    }


}
