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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoDustPlacer {
    public static void tryPlaceOnTop(PlayerEntity player, BlockPos blockpos, World world) {
        BlockPos dustPos = blockpos.up();
        BlockState floor = world.getBlockState(blockpos);
        if (world.getBlockState(dustPos).isAir() && (floor.isSideSolidFullSquare(world, blockpos, Direction.UP) || floor.isOf(Blocks.HOPPER))) {

            ItemPlacementContext context = new ItemPlacementContext(player, Hand.MAIN_HAND,
                    new ItemStack(Items.REDSTONE),
                    new BlockHitResult(
                            new Vec3d(dustPos.getX(), dustPos.getY(), dustPos.getZ())
                            , Direction.UP, dustPos, false)
            );

            player.getWorld().setBlockState(dustPos, Blocks.REDSTONE_WIRE.getPlacementState(context));
            player.getWorld().getBlockState(dustPos).onBlockAdded(world, dustPos, Blocks.REDSTONE_WIRE.getPlacementState(context), true);
            world.setBlockState(dustPos, Blocks.REDSTONE_WIRE.getDefaultState());
        }
    }
}
