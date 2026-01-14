/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Ryan100c and contributors
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

package mypals.ml.mixin.features.instantFalling;

import com.mojang.logging.LogUtils;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//#if MC > 12105
import net.minecraft.world.level.storage.TagValueInput;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlock.class)
public class BrushableBlockMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void scheduledTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!FallingBlock.isFree(world.getBlockState(pos.below())) || pos.getY() < world.getMinY()) {
            return;
        }
        if (YetAnotherCarpetAdditionRules.instantFalling) {
            BlockState blockState = world.getBlockState(pos);
            BlockEntity be = world.getBlockEntity(pos);
            CompoundTag beData = null;
            if (be != null) {
                //#if MC > 12004
                beData = be.saveWithoutMetadata(world.registryAccess());
                //#else
                //$$ beData = be.saveWithoutMetadata();
                //#endif
            }

            world.removeBlock(pos, false);
            BlockPos blockPos = pos.below();
            while (FallingBlock.isFree(world.getBlockState(blockPos)) && blockPos.getY() > world.getMinY()) {
                blockPos = blockPos.below();
            }
            if (blockPos.getY() >= world.getMinY()) {
                BlockPos targetPos = blockPos.above();
                world.setBlockAndUpdate(targetPos, blockState);
                if (beData != null) {
                    BlockEntity newBe = world.getBlockEntity(targetPos);
                    if (newBe != null) {
                        //#if MC > 12105
                        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(newBe.problemPath(), LogUtils.getLogger())) {
                            newBe.loadWithComponents(TagValueInput.create(scopedCollector, world.registryAccess(), beData));
                        }
                        //#elseif MC >12104
                        //$$ newBe.loadWithComponents(beData, world.registryAccess());
                        //#else
                        //$$ newBe.load(beData);
                        //#endif
                    }
                }
            }
            ci.cancel();
        }
    }
}

