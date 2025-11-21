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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
//#if MC <= 12006
import java.util.concurrent.Executor;
//#endif

@Mixin(FlatLevelSource.class)
public class FlatChunkGeneratorMixin {
    @Shadow
    @Final
    private FlatLevelGeneratorSettings settings;

    @Unique
    private static BlockState getChunkBlockState(ChunkAccess chunk, GridWorldGenerator.ChessboardSuperFlatSettings settings) {
        ChunkPos chunkPos = chunk.getPos();
        int size = settings.size;
        int groupX = Math.floorDiv(chunkPos.x, size);
        int groupZ = Math.floorDiv(chunkPos.z, size);
        boolean isBlack = (groupX + groupZ) % 2 == 0;
        return isBlack ? settings.black.defaultBlockState() : settings.white.defaultBlockState();
    }

    @WrapMethod(method = "fillFromNoise")
    public CompletableFuture<ChunkAccess> populateNoise(
            //#if MC <= 12006
            Executor executor,
            //#endif
            Blender blender, RandomState noiseConfig, StructureManager structureAccessor, ChunkAccess chunk, Operation<CompletableFuture<ChunkAccess>> original
    ) {
        if (!Objects.equals(YetAnotherCarpetAdditionRules.chessboardSuperFlatSettings, "off")) {

            GridWorldGenerator.ChessboardSuperFlatSettings settings = parseSettings(YetAnotherCarpetAdditionRules.chessboardSuperFlatSettings);

            BlockState blockState = getChunkBlockState(chunk, settings);

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            Heightmap heightmapOcean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
            List<BlockState> list = this.settings.getLayers();
            for (int y = 0; y < Math.min(chunk.getHeight(), list.size()); ++y) {
                if (blockState != null) {
                    int ytop = chunk.getMinBuildHeight() + y;

                    for (int x = 0; x < 16; ++x) {
                        for (int z = 0; z < 16; ++z) {
                            chunk.setBlockState(mutable.set(x, ytop, z), blockState
                                    //#if MC <= 12104
                                    , false
                                    //#endif
                            );
                            heightmapOcean.update(x, ytop, z, blockState);
                            heightmapSurface.update(x, ytop, z, blockState);
                        }
                    }
                }
            }

            return CompletableFuture.completedFuture(chunk);
        } else {
            return original.call(
                    //#if MC <= 12006
                    executor,
                    //#endif
                    blender, noiseConfig, structureAccessor, chunk
            );
        }
    }

    @Unique
    private static GridWorldGenerator.ChessboardSuperFlatSettings parseSettings(String settings) {
        String[] parts = settings.split(";");
        ResourceLocation blockId1 = ResourceLocation.tryParse(parts[0]);
        if (blockId1 == null) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    Blocks.WHITE_STAINED_GLASS,
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }
        Optional<Block> block1 = BuiltInRegistries.BLOCK.getOptional(blockId1);
        if (block1.isEmpty()) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    Blocks.WHITE_STAINED_GLASS,
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }

        ResourceLocation blockId2 = ResourceLocation.tryParse(parts[1]);
        if (blockId2 == null) {
            return new GridWorldGenerator.ChessboardSuperFlatSettings(
                    block1.get(),
                    Blocks.BLACK_STAINED_GLASS,
                    1
            );
        }
        Optional<Block> block2 = BuiltInRegistries.BLOCK.getOptional(blockId2);
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
