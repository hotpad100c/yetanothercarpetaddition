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

package mypals.ml.mixin.features.bedRecordHeadRotation;

import mypals.ml.interfaces.BedBlockEntityExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC <= 12004
//$$ import net.minecraft.world.InteractionHand;
//#endif

import static net.minecraft.world.level.block.BedBlock.PART;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {
    @Shadow
    private static Direction getNeighbourDirection(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Inject(
            //#if MC <= 12004
            //$$ method = "use",
            //#else
            method = "useWithoutItem",
            //#endif
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
            )
    )
    private void onUse(BlockState state, Level world, BlockPos pos, Player player,
                       //#if MC <= 12004
                       //$$ InteractionHand hand,
                       //#endif
                       BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing) {
            if (world.getBlockEntity(pos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYRot());
                bedBlockEntityPlus.getSleeperPitch(player.getXRot());
            }
            BedPart bedPart = state.getValue(PART);
            BlockPos blockPos = pos.relative(getNeighbourDirection(bedPart, state.getValue(FACING)));
            if (world.getBlockEntity(blockPos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYRot());
                bedBlockEntityPlus.getSleeperPitch(player.getXRot());
            }
        }
    }
}
