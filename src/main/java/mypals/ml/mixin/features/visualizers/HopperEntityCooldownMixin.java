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
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.phys.Vec3;

@Mixin(HopperBlockEntity.class)
public class HopperEntityCooldownMixin {
    @Inject(
            method = "pushItemsTick",
            at = @At("TAIL")
    )
    private static void ServerTickAddMarker(Level world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (world instanceof ServerLevel serverWorld && YetAnotherCarpetAdditionRules.hopperCooldownVisualize) {
            YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.setVisualizer(serverWorld, pos, Vec3.atCenterOf(pos), blockEntity.cooldownTime);
            BlockEntity blockEntity1 = world.getBlockEntity(pos
                    //#if MC >= 12006
                    .relative(blockEntity.facing)
                    //#endif
            );
            if (blockEntity1 instanceof HopperBlockEntity hopperblockentity) {
                YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.setVisualizer(serverWorld
                        //#if MC >= 12006
                        , pos.relative(blockEntity.facing)
                        , Vec3.atCenterOf(pos.relative(blockEntity.facing))
                        //#else
                        //$$ , pos
                        //$$ , Vec3.atCenterOf(pos)
                        //#endif
                        , hopperblockentity.cooldownTime);
            }
        }
    }
}
