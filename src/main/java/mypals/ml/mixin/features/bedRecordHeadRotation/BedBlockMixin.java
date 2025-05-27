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
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#if MC <= 12004
//$$ import net.minecraft.util.Hand;
//#endif

import static net.minecraft.block.BedBlock.PART;
import static net.minecraft.block.HorizontalFacingBlock.FACING;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {
    @Shadow
    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Inject(
            method = "onUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;"
            )
    )
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                       //#if MC <= 12004
                       //$$ Hand hand,
                       //#endif
                       BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing) {
            if (world.getBlockEntity(pos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYaw());
                bedBlockEntityPlus.getSleeperPitch(player.getPitch());
            }
            BedPart bedPart = state.get(PART);
            BlockPos blockPos = pos.offset(getDirectionTowardsOtherPart(bedPart, state.get(FACING)));
            if (world.getBlockEntity(blockPos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYaw());
                bedBlockEntityPlus.getSleeperPitch(player.getPitch());
            }
        }
    }
}
