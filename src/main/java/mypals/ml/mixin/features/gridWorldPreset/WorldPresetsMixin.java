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
import mypals.ml.features.GridWorldGen.FlatGridChunkGenerator;
import mypals.ml.features.GridWorldGen.GridWorldGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Optional;

import static net.minecraft.world.gen.WorldPresets.DEBUG_ALL_BLOCK_STATES;
import static net.minecraft.world.gen.WorldPresets.FLAT;
import static net.minecraft.world.gen.WorldPresets.DEFAULT;

@Mixin(WorldPresets.class)
public class WorldPresetsMixin {
    /*@WrapMethod(method = "getWorldPreset")
    private static Optional<RegistryKey<WorldPreset>> getWorldPreset(DimensionOptionsRegistryHolder registry, Operation<Optional<RegistryKey<WorldPreset>>> original) {
        return registry.getOrEmpty(DimensionOptions.OVERWORLD).flatMap(overworld -> switch (overworld.chunkGenerator()) {
            case FlatGridChunkGenerator flatGridChunkGenerator -> Optional.of(GridWorldGenerator.GRID);
            case FlatChunkGenerator flatChunkGenerator -> Optional.of(FLAT);
            case DebugChunkGenerator debugChunkGenerator -> Optional.of(DEBUG_ALL_BLOCK_STATES);
            case NoiseChunkGenerator noiseChunkGenerator -> Optional.of(DEFAULT);
            default -> Optional.empty();
        });
    }*/

}
