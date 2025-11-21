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

//#if MC >= 12109
import java.util.function.Consumer;
//#endif
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;

public class FakeEntries {
    public static record SimpleEntryFake(BlockPos pos, Block sourceBlock,
                                         //#if MC <= 12102
                                         //$$ BlockPos sourcePos
                                         //#else
                                         Orientation wireOrientation
                                         //#endif
    ) implements CollectingNeighborUpdater.NeighborUpdates {
        public SimpleEntryFake(BlockPos pos, Block sourceBlock,
                               //#if MC <= 12102
                               //$$ BlockPos sourcePos
                               //#else
                               Orientation wireOrientation
                               //#endif
        ) {
            this.pos = pos;
            this.sourceBlock = sourceBlock;
            //#if MC <= 12102
            //$$ this.sourcePos = sourcePos;
            //#else
            this.wireOrientation = wireOrientation;
            //#endif
        }

        public boolean runNext(Level world) {
            UpdateLoggerHelper.incrementNC();
            BlockState blockState = world.getBlockState(this.pos);
            NeighborUpdater.executeUpdate(world, blockState, this.pos, this.sourceBlock, this

                            //#if MC <= 12102
                            //$$ .sourcePos
                    //#else
                    .wireOrientation
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
        //$$ public BlockPos sourcePos() {
        //$$     return this.sourcePos;
        //$$ }
        //#else
        public Orientation wireOrientation() {return this.wireOrientation;}
        //#endif

        //#if MC >= 12109
        @Override
        public void forEachUpdatedPos(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }
        //#endif
    }

    public static record StateReplacementEntryFake(Direction direction, BlockState neighborState, BlockPos pos,
                                                   BlockPos neighborPos, int updateFlags,
                                                   int updateLimit) implements CollectingNeighborUpdater.NeighborUpdates {
        public StateReplacementEntryFake(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) {
            this.direction = direction;
            this.neighborState = neighborState;
            this.pos = pos;
            this.neighborPos = neighborPos;
            this.updateFlags = updateFlags;
            this.updateLimit = updateLimit;
        }

        public boolean runNext(Level world) {
            UpdateLoggerHelper.incrementPP();
            NeighborUpdater.executeShapeUpdate(world,
                    //#if MC <=12102
                    //$$ this.direction, this.neighborState, this.pos, this.neighborPos, this.updateFlags, this.updateLimit
                    //#else
                    this.direction, this.pos, this.neighborPos, this.neighborState, this.updateFlags, this.updateLimit
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

        //#if MC >= 12109
        @Override
        public void forEachUpdatedPos(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }
        //#endif
    }

    public static record StatefulEntryFake(BlockState state, BlockPos pos, Block sourceBlock,

                                           //#if MC <= 12102
                                           //$$ BlockPos sourcePos
                                           //#else
                                           Orientation wireOrientation
                                           //#endif
            ,
                                           boolean movedByPiston) implements CollectingNeighborUpdater.NeighborUpdates {
        public StatefulEntryFake(BlockState state, BlockPos pos, Block sourceBlock,

                                 //#if MC <= 12102
                                 //$$ BlockPos sourcePos
                                 //#else
                                 Orientation wireOrientation
                                 //#endif
                , boolean movedByPiston) {
            this.state = state;
            this.pos = pos;
            this.sourceBlock = sourceBlock;
            //#if MC <= 12102
            //$$ this.sourcePos = sourcePos;
            //#else
            this.wireOrientation = wireOrientation;
            //#endif
            this.movedByPiston = movedByPiston;
        }

        public boolean runNext(Level world) {
            UpdateLoggerHelper.incrementNC();
            NeighborUpdater.executeUpdate(world, this.state, this.pos, this.sourceBlock,
                    //#if MC <= 12102
                    //$$ this.sourcePos
                    //#else
                    this.wireOrientation
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
        //$$ public BlockPos sourcePos() {
        //$$     return this.sourcePos;
        //$$ }
        //#else
        public Orientation wireOrientation() {return this.wireOrientation;}
        //#endif

        public boolean movedByPiston() {
            return this.movedByPiston;
        }

        //#if MC >= 12109
        @Override
        public void forEachUpdatedPos(Consumer<BlockPos> callback) {
            callback.accept(this.pos);
        }
        //#endif
    }
}
