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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Mixin(StructureBlockBlockEntity.class)

public class StructureBlockMixin extends BlockEntity {
    @Shadow
    private StructureBlockMode mode;

    @Shadow
    private BlockPos offset;

    @Shadow
    private Vec3i size;

    @Shadow private Identifier templateName;

    public StructureBlockMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapMethod(method = "detectStructureSize")
    public boolean detectStructureSize(Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.optimizedStructureBlock) {
            return original.call();
        } else {
            if (this.mode != StructureBlockMode.SAVE) {
                return false;
            }

            BlockBox blockBox = new BlockBox(this.getPos());
            int radius = 80;
            AtomicBoolean foundValidStructure = new AtomicBoolean(false);

            forEachChunkInCube(this.pos, radius, chunkPos -> {

                Chunk chunk = this.world.getChunk(chunkPos.x, chunkPos.z);
                chunk.getBlockEntityPositions().forEach(pos -> {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be instanceof StructureBlockBlockEntity sb && sb.getMode() == StructureBlockMode.CORNER && Objects.equals(this.templateName.toString(), sb.getTemplateName())) {
                        blockBox.encompass(pos);
                        foundValidStructure.set(true);
                    }
                });
                if (foundValidStructure.get()) {
                    return;
                }
            });

            int dx = blockBox.getMaxX() - blockBox.getMinX();
            int dy = blockBox.getMaxY() - blockBox.getMinY();
            int dz = blockBox.getMaxZ() - blockBox.getMinZ();

            if (dx > 0 && dy > 0 && dz > 0) {
                this.offset = new BlockPos(
                        blockBox.getMinX() - this.getPos().getX()+1,
                        blockBox.getMinY() - this.getPos().getY()+1,
                        blockBox.getMinZ() - this.getPos().getZ()+1
                );
                this.size = new Vec3i(dx-1, dy-1, dz-1);


                this.markDirty();
                BlockState blockState = this.world.getBlockState(this.getPos());
                this.world.updateListeners(this.getPos(), blockState, blockState, 3);

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

