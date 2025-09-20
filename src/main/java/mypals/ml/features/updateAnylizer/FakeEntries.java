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

package mypals.ml.features.updateAnylizer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
//#if MC>12102
//$$ import net.minecraft.world.block.WireOrientation;
//#endif

public class FakeEntries {
    public static record SimpleEntryFake(BlockPos pos, Block sourceBlock,
                                         //#if MC <= 12102
                                         BlockPos sourcePos
                                         //#else
                                         //$$ WireOrientation wireOrientation
                                         //#endif
    ) implements ChainRestrictedNeighborUpdater.Entry {
        public SimpleEntryFake(BlockPos pos, Block sourceBlock,
                               //#if MC <= 12102
                               BlockPos sourcePos
                               //#else
                               //$$ WireOrientation wireOrientation
                               //#endif
        ) {
            this.pos = pos;
            this.sourceBlock = sourceBlock;
            //#if MC <= 12102
            this.sourcePos = sourcePos;
            //#else
            //$$ this.wireOrientation = wireOrientation;
            //#endif
        }

        public boolean update(World world) {
            UpdateLoggerHelper.incrementNC();
            BlockState blockState = world.getBlockState(this.pos);
            NeighborUpdater.tryNeighborUpdate(world, blockState, this.pos, this.sourceBlock, this

                            //#if MC <= 12102
                            .sourcePos
                    //#else
                    //$$.wireOrientation
                    //#endif

                    , false);
            return false;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Block sourceBlock() {
            return this.sourceBlock;
        }

        //#if MC <= 12102
        public BlockPos sourcePos() {
            return this.sourcePos;
        }
        //#else
        //$$ public WireOrientation wireOrientation() {return this.wireOrientation;}
        //#endif

    }

    public static record StateReplacementEntryFake(Direction direction, BlockState neighborState, BlockPos pos,
                                                   BlockPos neighborPos, int updateFlags,
                                                   int updateLimit) implements ChainRestrictedNeighborUpdater.Entry {
        public StateReplacementEntryFake(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) {
            this.direction = direction;
            this.neighborState = neighborState;
            this.pos = pos;
            this.neighborPos = neighborPos;
            this.updateFlags = updateFlags;
            this.updateLimit = updateLimit;
        }

        public boolean update(World world) {
            UpdateLoggerHelper.incrementPP();
            NeighborUpdater.replaceWithStateForNeighborUpdate(world,
                    //#if MC <=12102
                    this.direction, this.neighborState, this.pos, this.neighborPos, this.updateFlags, this.updateLimit
                    //#else
                    //$$ this.direction, this.pos, this.neighborPos, this.neighborState, this.updateFlags, this.updateLimit
                    //#endif

            );
            return false;
        }

        public Direction direction() {
            return this.direction;
        }

        public BlockState neighborState() {
            return this.neighborState;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockPos neighborPos() {
            return this.neighborPos;
        }

        public int updateFlags() {
            return this.updateFlags;
        }

        public int updateLimit() {
            return this.updateLimit;
        }
    }

    public static record StatefulEntryFake(BlockState state, BlockPos pos, Block sourceBlock,

                                           //#if MC <= 12102
                                           BlockPos sourcePos
                                           //#else
                                           //$$ WireOrientation wireOrientation
                                           //#endif
            ,
                                           boolean movedByPiston) implements ChainRestrictedNeighborUpdater.Entry {
        public StatefulEntryFake(BlockState state, BlockPos pos, Block sourceBlock,

                                 //#if MC <= 12102
                                 BlockPos sourcePos
                                 //#else
                                 //$$ WireOrientation wireOrientation
                                 //#endif
                , boolean movedByPiston) {
            this.state = state;
            this.pos = pos;
            this.sourceBlock = sourceBlock;
            //#if MC <= 12102
            this.sourcePos = sourcePos;
            //#else
            //$$ this.wireOrientation = wireOrientation;
            //#endif
            this.movedByPiston = movedByPiston;
        }

        public boolean update(World world) {
            UpdateLoggerHelper.incrementNC();
            NeighborUpdater.tryNeighborUpdate(world, this.state, this.pos, this.sourceBlock,
                    //#if MC <= 12102
                    this.sourcePos
                    //#else
                    //$$ this.wireOrientation
                    //#endif
                    , this.movedByPiston);
            return false;
        }

        public BlockState state() {
            return this.state;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Block sourceBlock() {
            return this.sourceBlock;
        }

        //#if MC <= 12102
        public BlockPos sourcePos() {
            return this.sourcePos;
        }
        //#else
        //$$ public WireOrientation wireOrientation() {return this.wireOrientation;}
        //#endif

        public boolean movedByPiston() {
            return this.movedByPiston;
        }
    }
}
