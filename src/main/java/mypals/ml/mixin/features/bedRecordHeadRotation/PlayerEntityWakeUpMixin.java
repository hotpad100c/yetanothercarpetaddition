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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing;
import static net.minecraft.block.BedBlock.PART;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerEntityWakeUpMixin extends PlayerEntity {


    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public PlayerEntityWakeUpMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }


    @Inject(
            method = "wakeUp(ZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void wakeUp(CallbackInfo ci) {
        BedBlockEntityExtension bed = findNearbyBeds(this.getBlockPos(), this.getWorld());
        if (bed != null && bedsRecordSleeperFacing) {
            this.networkHandler.requestTeleport(this.getX(), this.getY(), this.getZ(),
                    bed.getSleeperYaw(), bed.getSleeperPitch());
        }


    }

    @Unique
    private static BedBlockEntityExtension findNearbyBeds(BlockPos playerPos, World world) {
        for (BlockPos pos : BlockPos.iterate(playerPos.add(-1, -1, -1), playerPos.add(1, 1, 1))) {

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof BedBlockEntityExtension bedBlockEntity) {
                BedPart bedPart = world.getBlockState(pos).get(PART);
                if (bedPart == BedPart.HEAD) {
                    return bedBlockEntity;
                }
            }
        }
        return null;
    }
}
