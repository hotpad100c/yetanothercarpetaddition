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

package mypals.ml.mixin.features.optimizedStructureBlock;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Mixin(StructureBlockEntity.class)

public class StructureBlockMixin extends BlockEntity {
    @Shadow
    private StructureMode mode;

    @Shadow
    private BlockPos structurePos;

    @Shadow
    private Vec3i structureSize;

    @Shadow private ResourceLocation structureName;

    public StructureBlockMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapMethod(method = "detectSize")
    public boolean detectStructureSize(Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.optimizedStructureBlock) {
            return original.call();
        } else {
            if (this.mode != StructureMode.SAVE) {
                return false;
            }

            BoundingBox blockBox = new BoundingBox(this.getBlockPos());
            int radius = 80;
            AtomicBoolean foundValidStructure = new AtomicBoolean(false);

            forEachChunkInCube(this.worldPosition, radius, chunkPos -> {

                ChunkAccess chunk = this.level.getChunk(chunkPos.x, chunkPos.z);
                chunk.getBlockEntitiesPos().forEach(pos -> {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be instanceof StructureBlockEntity sb && sb.getMode() == StructureMode.CORNER && Objects.equals(this.structureName.toString(), sb.getStructureName())) {
                        blockBox.encapsulate(pos);
                        foundValidStructure.set(true);
                    }
                });
                if (foundValidStructure.get()) {
                    return;
                }
            });

            int dx = blockBox.maxX() - blockBox.minX();
            int dy = blockBox.maxY() - blockBox.minY();
            int dz = blockBox.maxZ() - blockBox.minZ();

            if (dx > 0 && dy > 0 && dz > 0) {
                this.structurePos = new BlockPos(
                        blockBox.minX() - this.getBlockPos().getX()+1,
                        blockBox.minY() - this.getBlockPos().getY()+1,
                        blockBox.minZ() - this.getBlockPos().getZ()+1
                );
                this.structureSize = new Vec3i(dx-1, dy-1, dz-1);


                this.setChanged();
                BlockState blockState = this.level.getBlockState(this.getBlockPos());
                this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);

                return true;
            }

            return false;
        }
    }

    @Unique
    private static void forEachChunkInCube(BlockPos center, int radius, Consumer<ChunkPos> action) {
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = center.getY() - radius;
        int maxY = center.getY() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                action.accept(new ChunkPos(chunkX, chunkZ));
            }
        }
    }
}

