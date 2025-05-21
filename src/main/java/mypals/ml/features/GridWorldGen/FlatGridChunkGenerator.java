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

package mypals.ml.features.GridWorldGen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.concurrent.CompletableFuture;

public class FlatGridChunkGenerator extends FlatChunkGenerator {
    public static final MapCodec<FlatGridChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    FlatChunkGeneratorConfig.CODEC.fieldOf("settings").forGetter(FlatChunkGenerator::getConfig)
            ).apply(instance, instance.stable(FlatGridChunkGenerator::new))
    );

    public FlatGridChunkGenerator(FlatChunkGeneratorConfig config) {
        super(config);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        boolean isBlack = (chunkPos.x + chunkPos.z) % 2 == 0;
        BlockState blockState = isBlack ? Blocks.BLACK_STAINED_GLASS.getDefaultState() : Blocks.WHITE_STAINED_GLASS.getDefaultState();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap heightmapOcean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        for (int y = chunk.getBottomY(); y < chunk.getTopY(); ++y) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    chunk.setBlockState(mutable.set(x, y, z), blockState
                            //#if MC < 12105
                            , false
                            //#endif
                    );
                    heightmapOcean.trackUpdate(x, y, z, blockState);
                    heightmapSurface.trackUpdate(x, y, z, blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }
}