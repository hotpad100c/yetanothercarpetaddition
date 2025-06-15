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


import net.minecraft.block.Block;

public class GridWorldGenerator {
    /*public static final RegistryKey<WorldPreset> GRID = GridWorldGenerator.of("grid_world");


    private static RegistryKey<WorldPreset> of(String id) {
        return RegistryKey.of(RegistryKeys.WORLD_PRESET, Identifier.of(MOD_ID, id));
    }

    public static void init() {
        //Registry.register(Registries.TYPE, GridWorldGenerator.GRID, WorldPresetTags.NORMAL);
    }*/
    public static class ChessboardSuperFlatSettings {
        public Block white;
        public Block black;
        public int size;

        public ChessboardSuperFlatSettings(Block white, Block black, int size) {
            this.white = white;
            this.black = black;
            this.size = size;
        }
    }
}
