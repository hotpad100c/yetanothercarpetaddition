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

package mypals.ml.features.treeGrowthStats;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class WorldSnapshot {

    public static final int RADIUS_XZ = 32;
    public static final int BELOW = 16;
    public static final int ABOVE = 64;

    public final int minX;
    public final int minY;
    public final int minZ;
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final BlockState[] states;
    public final RegistryAccess registryAccess;
    public final ChunkGenerator generator;
    public final Holder<Biome> biome;
    public final long seed;
    public final int[] surface;

    private WorldSnapshot(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ,
                          BlockState[] states, RegistryAccess registryAccess,
                          ChunkGenerator generator, Holder<Biome> biome, long seed,
                          int[] surface) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.states = states;
        this.registryAccess = registryAccess;
        this.generator = generator;
        this.biome = biome;
        this.seed = seed;
        this.surface = surface;
    }

    public static WorldSnapshot capture(ServerLevel level, BlockPos origin) {
        int minX = origin.getX() - RADIUS_XZ;
        int minZ = origin.getZ() - RADIUS_XZ;
        int minY = origin.getY() - BELOW;
        int sizeX = RADIUS_XZ * 2 + 1;
        int sizeZ = sizeX;
        int sizeY = BELOW + ABOVE + 1;
        BlockState[] states = new BlockState[sizeX * sizeY * sizeZ];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                int base = (dx * sizeY + dy) * sizeZ;
                for (int dz = 0; dz < sizeZ; dz++) {
                    cursor.set(minX + dx, minY + dy, minZ + dz);
                    states[base + dz] = level.getBlockState(cursor);
                }
            }
        }
        int[] surface = new int[sizeX * sizeZ];
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dz = 0; dz < sizeZ; dz++) {
                int top = minY;
                for (int dy = sizeY - 1; dy >= 0; dy--) {
                    if (!states[(dx * sizeY + dy) * sizeZ + dz].isAir()) {
                        top = minY + dy + 1;
                        break;
                    }
                }
                surface[dx * sizeZ + dz] = top;
            }
        }
        return new WorldSnapshot(minX, minY, minZ, sizeX, sizeY, sizeZ, states,
                level.registryAccess(),
                level.getChunkSource().getGenerator(),
                level.getBiome(origin),
                level.getSeed(),
                surface);
    }

    public int surfaceY(int x, int z) {
        int dx = x - minX;
        int dz = z - minZ;
        if (dx < 0 || dz < 0 || dx >= sizeX || dz >= sizeZ) {
            return minY;
        }
        return surface[dx * sizeZ + dz];
    }

    public int index(int x, int y, int z) {
        int dx = x - minX;
        int dy = y - minY;
        int dz = z - minZ;
        if (dx < 0 || dy < 0 || dz < 0 || dx >= sizeX || dy >= sizeY || dz >= sizeZ) {
            return -1;
        }
        return (dx * sizeY + dy) * sizeZ + dz;
    }

    public int cellCount() {
        return sizeX * sizeY * sizeZ;
    }
}
