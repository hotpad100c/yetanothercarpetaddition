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

package mypals.ml.mixin.features.gridWorldPreset;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.features.GridWorldGen.GridWorldGenerator;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin {
    @Shadow
    @Final
    private FlatChunkGeneratorConfig config;

    @Unique
    private static BlockState getChunkBlockState(Chunk chunk, GridWorldGenerator.ChessboardSuperFlatSettings settings) {
        ChunkPos chunkPos = chunk.getPos();
        int size = settings.size;
        int groupX = Math.floorDiv(chunkPos.x, size);
        int groupZ = Math.floorDiv(chunkPos.z, size);
        boolean isBlack = (groupX + groupZ) % 2 == 0;
        return isBlack ? settings.black.getDefaultState() : settings.white.getDefaultState();
    }

    @WrapMethod(method = "populateNoise")
    public CompletableFuture<Chunk> populateNoise(
            Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk, Operation<CompletableFuture<Chunk>> original
    ) {
        if (!Objects.equals(YetAnotherCarpetAdditionRules.chessboardSuperFlatSettings, "off")) {

            GridWorldGenerator.ChessboardSuperFlatSettings settings = parseSettings(YetAnotherCarpetAdditionRules.chessboardSuperFlatSettings);

            BlockState blockState = getChunkBlockState(chunk, settings);

            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Heightmap heightmapOcean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
            Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
            List<BlockState> list = this.config.getLayerBlocks();
            for (int y = 0; y < Math.min(chunk.getHeight(), list.size()); ++y) {
                if (blockState != null) {
                    int ytop = chunk.getBottomY() + y;

                    for (int x = 0; x < 16; ++x) {
                        for (int z = 0; z < 16; ++z) {
                            chunk.setBlockState(mutable.set(x, ytop, z), blockState
                                    //#if MC <= 12104
                                    , false
                                    //#endif
                            );
                            heightmapOcean.trackUpdate(x, ytop, z, blockState);
                            heightmapSurface.trackUpdate(x, ytop, z, blockState);
                        }
                    }
                }
            }

            return CompletableFuture.completedFuture(chunk);
        } else {
            return original.call(blender, noiseConfig, structureAccessor, chunk);
        }
    }

    @Unique
    private static GridWorldGenerator.ChessboardSuperFlatSettings parseSettings(String settings) {
        String[] parts = settings.split(";");
        Identifier blockId1 = Identifier.tryParse(parts[0]);
        if (blockId1 == null) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    Blocks.WHITE_STAINED_GLASS,
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }
        Optional<Block> block1 = Registries.BLOCK.getOrEmpty(blockId1);
        if (block1.isEmpty()) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    Blocks.WHITE_STAINED_GLASS,
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }

        Identifier blockId2 = Identifier.tryParse(parts[1]);
        if (blockId2 == null) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    block1.get(),
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }
        Optional<Block> block2 = Registries.BLOCK.getOrEmpty(blockId2);
        if (block2.isEmpty()) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    block1.get(),
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }
        int value;
        try {
            value = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    block1.get(),
                    block2.get(),
                    1
            );
        }
        return new GridWorldGenerator.ChessboardSuperFlatSettings(
                block1.get(),
                block2.get(),
                value
        );

    }
}
