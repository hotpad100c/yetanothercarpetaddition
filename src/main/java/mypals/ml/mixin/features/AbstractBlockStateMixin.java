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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(
            method = "onBlockAdded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBlockAdded(World world, BlockPos pos, BlockState state, boolean notify, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "prepare*",
            at = @At("HEAD"),
            cancellable = true
    )
    public void prepare(WorldAccess world, BlockPos pos, int flags, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "shouldSuffocate",
            at = @At("HEAD"),
            cancellable = true
    )
    public void shouldSuffocate(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoSuffocate) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "hasBlockBreakParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    public void hasBlockBreakParticles(CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blockNoBreakParticles) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "getHardness",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoHardness) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(
            method = "canPlaceAt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void canPlaceAt(CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blocksPlaceAtAnywhere) {
            cir.setReturnValue(true);
        }
    }

}
