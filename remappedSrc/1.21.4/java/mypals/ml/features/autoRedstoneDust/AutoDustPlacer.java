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

package mypals.ml.features.autoRedstoneDust;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoDustPlacer {
    public static void tryPlaceOnTop(Player player, BlockPos blockpos, Level world) {
        BlockPos dustPos = blockpos.above();
        BlockState floor = world.getBlockState(blockpos);
        if (world.getBlockState(dustPos).isAir() && (floor.isFaceSturdy(world, blockpos, Direction.UP) || floor.is(Blocks.HOPPER))) {

            BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND,
                    new ItemStack(Items.REDSTONE),
                    new BlockHitResult(
                            new Vec3(dustPos.getX(), dustPos.getY(), dustPos.getZ())
                            , Direction.UP, dustPos, false)
            );

            player.level().setBlockAndUpdate(dustPos, Blocks.REDSTONE_WIRE.getStateForPlacement(context));
            player.level().getBlockState(dustPos).onPlace(world, dustPos, Blocks.REDSTONE_WIRE.getStateForPlacement(context), true);
        }
    }
}
