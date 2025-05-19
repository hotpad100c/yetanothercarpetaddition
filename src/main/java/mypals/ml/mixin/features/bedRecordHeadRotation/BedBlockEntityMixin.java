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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedBlockEntity.class)
@Implements(@Interface(iface = BedBlockEntityExtension.class, prefix = "YACA$"))
public abstract class BedBlockEntityMixin extends BlockEntity {
    @Unique
    private float yaw, pitch = 0;

    public BedBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Unique
    public float YACA$getSleeperYaw() {
        return yaw;
    }

    @Unique
    public float YACA$getSleeperPitch() {
        return pitch;
    }

    @Unique
    public void YACA$setSleeperYaw(float yaw) {
        this.yaw = yaw;
    }

    @Unique
    public void YACA$getSleeperPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putFloat("SleeperYaw", this.yaw);
        nbt.putFloat("SleeperPitch", this.pitch);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.yaw = nbt.getFloat("SleeperYaw");
        this.pitch = nbt.getFloat("SleeperPitch");
    }
}
