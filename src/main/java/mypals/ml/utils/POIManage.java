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

package mypals.ml.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class POIManage {
    @Unique
    public static Stream<PointOfInterest> getPOIsWithinRange(ServerPlayerEntity player, ServerWorld world, int range) {
        ChunkPos playerChunkPos = new ChunkPos(player.getBlockPos());
        int chunkRadius = (int) Math.ceil(range / 16.0);
        return ChunkPos.stream(playerChunkPos, chunkRadius)
                .flatMap(chunkPos -> getAllInChunk(chunkPos, world))
                .filter(poi -> poi.getPos().getManhattanDistance(player.getBlockPos()) <= 50);
    }

    @Unique
    private static Stream<PointOfInterest> getAllInChunk(ChunkPos chunkPos, ServerWorld world) {
        return IntStream.range(world.getBottomSectionCoord(), world.getTopSectionCoord())
                .boxed()
                .map(integer -> world.getPointOfInterestStorage().get(ChunkSectionPos.from(chunkPos, integer).asLong()))
                .filter(Optional::isPresent)
                .flatMap(optional -> getAll(optional.get()));
    }

    @Unique
    private static Stream<PointOfInterest> getAll(PointOfInterestSet pointsOfInterestSet) {
        return pointsOfInterestSet.pointsOfInterestByType.values().stream()
                .flatMap(Set::stream);
    }
}
