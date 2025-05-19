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

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.features.visualizingFeatures.HopperCooldownVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class ChainRestrictedNeighborUpdaterMixin {
    @Shadow
    @Final
    private World world;

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerSimple(BlockPos pos, Block sourceBlock, BlockPos sourcePos, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.blockUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.NC);
    }

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerStateful(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.PP);
    }

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/Direction;)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerSixWayEntry(BlockPos pos, Block sourceBlock, Direction except, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.blockUpdateVisualize || this.world.isClient) return;
        for (Direction dir : NeighborUpdater.UPDATE_ORDER) {
            if (!(except != null && dir == except)) {
                YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos.offset(dir), BlockUpdateVisualizing.UpdateType.NC);
            }
        }

    }

    @Inject(
            method = "replaceWithStateForNeighborUpdate",
            at = @At("HEAD")
    )
    private void AddNCMarker(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        //if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || this.world.isClient) return;
        //YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.PP);

    }
}
