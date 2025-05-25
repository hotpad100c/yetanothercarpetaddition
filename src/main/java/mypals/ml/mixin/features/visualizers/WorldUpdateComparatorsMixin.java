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

package mypals.ml.mixin.features.visualizers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
//#if MC >= 12102
//$$ import net.minecraft.world.block.WireOrientation;
//#endif

@Mixin(World.class)
public abstract class WorldUpdateComparatorsMixin {
    @Shadow
    public abstract boolean isClient();

    @WrapOperation(
            method = "updateComparators",
            at = @At(
                    //#if MC < 12102
                    target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
                    //#else
                    //$$ target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V",
                    //#endif
                    ordinal = 0, value = "INVOKE"
            )
    )
    private void AddCPMarkerSimple(World instance, BlockState state, BlockPos pos, Block sourceBlock,
                                   //#if MC < 12102
                                   BlockPos sourcePos,
                                   //#else
                                   //$$ WireOrientation orientation,
                                   //#endif
                                   boolean notify, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.comparatorUpdateVisualize && !this.isClient())
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) this, pos, BlockUpdateVisualizing.UpdateType.CP);
        original.call(
                instance, state, pos, sourceBlock,
                //#if MC < 12102
                sourcePos,
                //#else
                //$$ orientation,
                //#endif
                notify
        );
    }

    @WrapOperation(
            method = "updateComparators",
            at = @At(
                    //#if MC < 12102
                    target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
                    //#else
                    //$$ target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V",
                    //#endif
                    ordinal = 1, value = "INVOKE"
            )
    )
    private void AddCPMarkerThroughBlocks(World instance, BlockState state, BlockPos pos, Block sourceBlock,
                                          //#if MC < 12102
                                          BlockPos sourcePos,
                                          //#else
                                          //$$ WireOrientation orientation,
                                          //#endif
                                          boolean notify, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.comparatorUpdateVisualize && !this.isClient())
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) this, pos, BlockUpdateVisualizing.UpdateType.CP);
        original.call(instance, state, pos, sourceBlock,
                //#if MC < 12102
                sourcePos,
                //#else
                //$$ orientation,
                //#endif
                notify);
    }
}
