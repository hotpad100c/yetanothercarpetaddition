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

package mypals.ml.mixin.features.treeGrowthStats;

import mypals.ml.features.treeGrowthStats.SampleContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelSnapshotMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        SampleContext ctx = SampleContext.get();
        if (ctx != null) {
            cir.setReturnValue(ctx.getBlockState(pos));
        }
    }

    //#if MC >= 260300
    //$$ @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    //$$ private void yaca$snapshotSetBlock(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
    //#else
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotSetBlock(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
    //#endif
        SampleContext ctx = SampleContext.get();
        if (ctx != null) {
            ctx.setBlock(pos, state);
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotGetFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        SampleContext ctx = SampleContext.get();
        if (ctx != null) {
            cir.setReturnValue(ctx.getBlockState(pos).getFluidState());
        }
    }

    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotGetBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (SampleContext.get() != null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getRandom", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotGetRandom(CallbackInfoReturnable<RandomSource> cir) {
        SampleContext ctx = SampleContext.get();
        if (ctx != null) {
            RandomSource source = ctx.random();
            if (source != null) {
                cir.setReturnValue(source);
            }
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void yaca$snapshotGetHeight(Heightmap.Types type, int x, int z, CallbackInfoReturnable<Integer> cir) {
        SampleContext ctx = SampleContext.get();
        if (ctx != null) {
            cir.setReturnValue(ctx.surfaceY(x, z));
        }
    }
}
