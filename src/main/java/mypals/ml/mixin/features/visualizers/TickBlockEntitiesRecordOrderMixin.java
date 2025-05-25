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

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class TickBlockEntitiesRecordOrderMixin {
    @Inject(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"),
            cancellable = true
    )
    private void blockTickBlockEntities(CallbackInfo ci, @Local BlockEntityTickInvoker blockEntityTickInvoker) {
        if ((World) (Object) this instanceof ServerWorld serverWorld && YetAnotherCarpetAdditionRules.blockEntityOrderVisualize) {
            YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.globalOrder++;
            YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.setVisualizer(serverWorld,
                    blockEntityTickInvoker.getPos(),
                    blockEntityTickInvoker.getPos().toCenterPos(),
                    YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.globalOrder);
        }
    }

    @Inject(
            method = "removeBlockEntity",
            at = @At("HEAD")
    )
    private void ServerTickAddScheduledTickMarker(BlockPos pos, CallbackInfo ci) {
        if ((World) (Object) this instanceof ServerWorld && YetAnotherCarpetAdditionRules.blockEntityOrderVisualize) {
            YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.removeVisualizer(pos);
        }
    }
}
