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

package mypals.ml.mixin.features.FallingSnowLayers;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 12102
//$$ import net.minecraft.world.WorldView;
//$$ import net.minecraft.world.tick.ScheduledTickView;
//#endif

@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin extends Block {
    public SnowLayerBlockMixin(Properties settings) {
        super(settings);
    }

    @WrapMethod(method = "updateShape")
    protected BlockState getStateForNeighborUpdate(BlockState state,
                                                   //#if MC >= 12102
                                                   //$$ WorldView world, ScheduledTickView tickView, BlockPos pos,
                                                   //$$ Direction direction,
                                                   //#else
                                                   Direction direction, BlockState neighborState,
                                                   LevelAccessor world, BlockPos pos,
                                                   //#endif
                                                   BlockPos neighborPos,
                                                   //#if MC >= 12102
                                                   //$$ BlockState neighborState, Random random,
                                                   //#endif
                                                   Operation<BlockState> original) {
        if (YetAnotherCarpetAdditionRules.fallingSnowLayers
        ) {
            if (!state.canSurvive(world, pos)) {
                //#if MC < 12102
                world
                //#else
                //$$ tickView
                //#endif
                        .scheduleTick(pos, world.getBlockState(pos).getBlock(), 2);
            }
            return
                    super
                    //#if MC < 12102
                            .updateShape(state, direction, neighborState, world, pos, neighborPos);
                    //#else
                    //$$    .getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
                    //#endif
        } else {
            return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super
                    //#if MC < 12102
                            .updateShape(state, direction, neighborState, world, pos, neighborPos);
                    //#else
                    //$$    .getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
                    //#endif

        }
    }

    @Override
    //#if MC >= 12006
    //$$ protected
    //#else
    public
    //#endif
    void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (YetAnotherCarpetAdditionRules.fallingSnowLayers
        ) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(world, pos, state);

        }
    }
}

