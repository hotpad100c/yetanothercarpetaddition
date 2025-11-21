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

package mypals.ml.mixin.features.updateLogger;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.updateAnylizer.FakeEntries;
import mypals.ml.features.updateAnylizer.UpdateLoggerHelper;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
//#if MC > 12102
import net.minecraft.world.block.WireOrientation;
//#endif

@Mixin(ChainRestrictedNeighborUpdater.class)
public abstract class ChainRestrictedNeighborUpdaterMixin implements NeighborUpdater {

    @Shadow
    protected abstract void enqueue(BlockPos pos, ChainRestrictedNeighborUpdater.Entry entry);

    @WrapMethod(method = "updateNeighbors")
    public void updateNeighbors(BlockPos pos, Block sourceBlock, Direction except,
                                //#if MC > 12102
                                WireOrientation wireOruentation,
                                //#endif
                                Operation<Void> original) {

        if (UpdateLoggerHelper.isUpdating() || !YetAnotherCarpetAdditionRules.updateCounter) {
            original.call(pos, sourceBlock,
                    except
                    //#if MC > 12102
                    ,wireOruentation
                    //#endif
            );
        } else {
            UpdateLoggerHelper.start();
            original.call(pos, sourceBlock,
                    except
                    //#if MC > 12102
                    ,wireOruentation
                    //#endif
            );
            UpdateLoggerHelper.finish();
        }
    }


    @WrapMethod(method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;" +
            //#if MC > 12102
            "Lnet/minecraft/world/block/WireOrientation;"
            //#else
            //$$ "Lnet/minecraft/util/math/BlockPos;"
            //#endif
            + ")V")
    public void updateNeighborA(BlockPos pos, Block sourceBlock,
                                //#if MC > 12102
                                WireOrientation orientation,
                                //#else
                                //$$ BlockPos sourcePos,
                                //#endif
                                Operation<Void> original) {
        if (UpdateLoggerHelper.isUpdating() || !YetAnotherCarpetAdditionRules.updateCounter) {
            original.call(pos, sourceBlock,
                    //#if MC > 12102
                    orientation
                    //#else
                    //$$ sourcePos
                    //#endif

            );
        } else {
            UpdateLoggerHelper.start();
            original.call(pos, sourceBlock,
                    //#if MC > 12102
                    orientation
                    //#else
                    //$$ sourcePos
                    //#endif
            );
            UpdateLoggerHelper.finish();
        }

    }


    @WrapMethod(method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;" +
            //#if MC > 12102
            "Lnet/minecraft/world/block/WireOrientation;"
            //#else
            //$$ "Lnet/minecraft/util/math/BlockPos;"
            //#endif
            + "Z)V")
    public void updateNeighborB(BlockState state, BlockPos pos, Block sourceBlock,
                                //#if MC > 12102
                                WireOrientation orientation
                                //#else
                                //$$ BlockPos sourcePos
                                //#endif
            , boolean notify, Operation<Void> original) {
        if (UpdateLoggerHelper.isUpdating() || !YetAnotherCarpetAdditionRules.updateCounter) {
            original.call(state, pos, sourceBlock,
                    //#if MC > 12102
                    orientation
                    //#else
                    //$$ sourcePos
                    //#endif
                    , notify);
        } else {
            UpdateLoggerHelper.start();
            original.call(state, pos, sourceBlock,
                    //#if MC > 12102
                    orientation
                    //#else
                    //$$ sourcePos
                    //#endif
                    , notify);
            UpdateLoggerHelper.finish();
        }

    }


    @WrapOperation(
            method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;" +
                    //#if MC > 12102
                    "Lnet/minecraft/world/block/WireOrientation;"
                    //#else
                    //$$ "Lnet/minecraft/util/math/BlockPos;"
                    //#endif
                    + ")V"

            , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;enqueue(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;)V"))
    public void updateNeighbor(ChainRestrictedNeighborUpdater instance, BlockPos pos, ChainRestrictedNeighborUpdater.Entry entry, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.updateCounter && entry instanceof ChainRestrictedNeighborUpdater.SimpleEntry simpleEntry) {
            this.enqueue(pos, new FakeEntries.SimpleEntryFake(pos, simpleEntry.sourceBlock(),
                    simpleEntry
                            //#if MC <= 12102
                            //$$ .sourcePos()
                    //#else
                    .orientation()
                    //#endif
            ));
        } else {
            original.call(instance, pos, entry);
        }

    }

    @WrapOperation(method = "replaceWithStateForNeighborUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;enqueue(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;)V"))
    public void updateNeighborPP(ChainRestrictedNeighborUpdater instance, BlockPos pos, ChainRestrictedNeighborUpdater.Entry entry, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.updateCounter && entry instanceof ChainRestrictedNeighborUpdater.StateReplacementEntry stateReplacementEntry) {
            this.enqueue(pos, new FakeEntries.StateReplacementEntryFake(stateReplacementEntry.direction(), stateReplacementEntry.neighborState(),
                    stateReplacementEntry.pos(), stateReplacementEntry.neighborPos(), stateReplacementEntry.updateFlags(), stateReplacementEntry.updateLimit()));
        } else {
            original.call(instance, pos, entry);
        }

    }

    @WrapOperation(
            method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;" +
                    //#if MC > 12102
                    "Lnet/minecraft/world/block/WireOrientation;"
                    //#else
                    //$$ "Lnet/minecraft/util/math/BlockPos;"
                    //#endif
                    + "Z)V",

            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;enqueue(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;)V"))
    public void updateNeighbor2(ChainRestrictedNeighborUpdater instance, BlockPos pos, ChainRestrictedNeighborUpdater.Entry entry, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.updateCounter && entry instanceof ChainRestrictedNeighborUpdater.StatefulEntry statefulEntry) {
            this.enqueue(pos, new FakeEntries.StatefulEntryFake(statefulEntry.state(), statefulEntry.pos(), statefulEntry.sourceBlock(), statefulEntry
                    //#if MC <= 12102
                    //$$ .sourcePos()
                    //#else
                    .orientation()
                    //#endif
                    , statefulEntry.movedByPiston()));
        } else {
            original.call(instance, pos, entry);
        }
    }

}
