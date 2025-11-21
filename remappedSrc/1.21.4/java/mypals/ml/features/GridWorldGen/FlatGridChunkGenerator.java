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

//#if MC >= 12006
import com.mojang.serialization.MapCodec;
//#else
//$$ import com.mojang.serialization.Codec;
//#endif
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.concurrent.CompletableFuture;
//#if MC < 12101
//$$ import java.util.concurrent.Executor;
//#endif
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class FlatGridChunkGenerator extends FlatLevelSource {
    public static final
    //#if MC >= 12006
    MapCodec<FlatGridChunkGenerator>
    //#else
    //$$ Codec<FlatGridChunkGenerator>
    //#endif

            CODEC = RecordCodecBuilder
            //#if MC >= 12006
            .mapCodec(
            //#else
            //$$ .create(
            //#endif
            instance -> instance.group(
                    FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings)
            ).apply(instance, instance.stable(FlatGridChunkGenerator::new))
    );

    public FlatGridChunkGenerator(FlatLevelGeneratorSettings config) {
        super(config);
    }

    @Override
    protected
    //#if MC >= 12006
    MapCodec<? extends ChunkGenerator>
    //#else
    //$$ Codec<? extends ChunkGenerator>
    //#endif
    codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            //#if MC < 12101
            //$$ Executor executor,
            //#endif
            Blender blender, RandomState noiseConfig, StructureManager structureAccessor, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        boolean isBlack = (chunkPos.x + chunkPos.z) % 2 == 0;
        BlockState blockState = isBlack ? Blocks.BLACK_STAINED_GLASS.defaultBlockState() : Blocks.WHITE_STAINED_GLASS.defaultBlockState();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Heightmap heightmapOcean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int y = chunk.getMinY(); y < chunk.getMaxY(); ++y) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    chunk.setBlockState(mutable.set(x, y, z), blockState
                            //#if MC < 12105
                            , false
                            //#endif
                    );
                    heightmapOcean.update(x, y, z, blockState);
                    heightmapSurface.update(x, y, z, blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }
}