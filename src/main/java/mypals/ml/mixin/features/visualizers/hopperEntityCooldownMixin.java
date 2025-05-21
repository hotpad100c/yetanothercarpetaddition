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

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.HopperCooldownVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class hopperEntityCooldownMixin {
    @Inject(
            method = "serverTick",
            at = @At("TAIL")
    )
    private static void ServerTickAddMarker(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld && YetAnotherCarpetAdditionRules.hopperCooldownVisualize) {
            YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.setVisualizer(serverWorld, pos, pos.toCenterPos(), blockEntity.transferCooldown);
            BlockEntity blockEntity1 = world.getBlockEntity(pos.offset(blockEntity.facing));
            if (blockEntity1 instanceof HopperBlockEntity hopperblockentity) {
                YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.setVisualizer(serverWorld, pos.offset(blockEntity.facing), pos.offset(blockEntity.facing).toCenterPos(), hopperblockentity.transferCooldown);
            }
        }
    }
}
