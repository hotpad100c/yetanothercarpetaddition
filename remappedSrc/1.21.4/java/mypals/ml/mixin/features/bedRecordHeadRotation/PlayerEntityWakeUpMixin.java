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

import com.mojang.authlib.GameProfile;
import mypals.ml.interfaces.BedBlockEntityExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing;
import static net.minecraft.world.level.block.BedBlock.PART;

@Mixin(ServerPlayer.class)
public abstract class PlayerEntityWakeUpMixin extends Player {


    @Shadow
    public ServerGamePacketListenerImpl connection;

    //#if MC >= 12106
    //$$ public PlayerEntityWakeUpMixin(World world, GameProfile gameProfile) {
    //$$     super(world, gameProfile);
    //$$ }
    //#else
    public PlayerEntityWakeUpMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {

        super(world, pos, yaw, gameProfile);
    }
    //#endif


    @Inject(
            method = "stopSleepInBed(ZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void wakeUp(CallbackInfo ci) {
        BedBlockEntityExtension bed = findNearbyBeds(this.blockPosition(), this.level());
        if (bed != null && bedsRecordSleeperFacing) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(),
                    bed.getSleeperYaw(), bed.getSleeperPitch());
        }


    }

    @Unique
    private static BedBlockEntityExtension findNearbyBeds(BlockPos playerPos, Level world) {
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-1, -1, -1), playerPos.offset(1, 1, 1))) {

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof BedBlockEntityExtension bedBlockEntity) {
                BedPart bedPart = world.getBlockState(pos).getValue(PART);
                if (bedPart == BedPart.HEAD) {
                    return bedBlockEntity;
                }
            }
        }
        return null;
    }
}
