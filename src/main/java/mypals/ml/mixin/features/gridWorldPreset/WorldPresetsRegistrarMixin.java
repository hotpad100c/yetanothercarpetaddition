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

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldPresets.Bootstrap.class)
public abstract class WorldPresetsRegistrarMixin {
    @Shadow
    protected abstract void registerCustomOverworldPreset(ResourceKey<WorldPreset> key, LevelStem dimensionOptions);

    @Shadow
    @Final
    private HolderGetter<Biome> biomes;

    @Shadow
    @Final
    private HolderGetter<StructureSet> structureSets;

    @Shadow
    @Final
    private HolderGetter<PlacedFeature> placedFeatures;

    @Shadow
    protected abstract LevelStem makeOverworld(ChunkGenerator chunkGenerator);

    //private static final RegistryKey<WorldPreset> GRID = of("grid_world");

    @Inject(method = "bootstrap()V", at = @At("RETURN"))
    private void addCustomPresets(CallbackInfo ci) {
        /*this.register(GRID, this.createOverworldOptions(
                new FlatGridChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(
                        this.biomeLookup, this.structureSetLookup, this.featureLookup
                ))
        ));*/
    }


}
