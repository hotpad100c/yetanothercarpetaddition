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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static net.minecraft.world.level.levelgen.presets.WorldPresets.DEBUG;
import static net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT;
import static net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL;

@Mixin(WorldPresets.Bootstrap.class)
public abstract class WorldPresetsMixin {
    @Shadow
    protected abstract void registerCustomOverworldPreset(ResourceKey<WorldPreset> key, LevelStem dimensionOptions);

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
