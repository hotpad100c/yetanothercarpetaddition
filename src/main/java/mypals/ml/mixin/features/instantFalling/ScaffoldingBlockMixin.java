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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScaffoldingBlock.class)
public class ScaffoldingBlockMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;fall(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/entity/item/FallingBlockEntity;"))
    private FallingBlockEntity scheduledTick(Level world, BlockPos pos, BlockState blockState, Operation<FallingBlockEntity> original) {
        if (!FallingBlock.isFree(world.getBlockState(pos.below())) || pos.getY() < world.getMinY()) {
            original.call(world, pos, blockState);
        }
        if (YetAnotherCarpetAdditionRules.instantFalling) {
            world.removeBlock(pos, false);
            BlockPos blockPos = pos.below();
            while (FallingBlock.isFree(world.getBlockState(blockPos)) && blockPos.getY() > world.getMinY()) {
                blockPos = blockPos.below();
            }
            if (blockPos.getY() >= world.getMinY()) {
                world.setBlockAndUpdate(blockPos.above(), blockState);
            }
        }
        return null;
    }
}
