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

import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.level.ChunkPos;

public class POIManage {
    @Unique
    public static Stream<PoiRecord> getPOIsWithinRange(ServerPlayer player, ServerLevel world, int range) {
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
        int chunkRadius = (int) Math.ceil(range / 16.0);
        return ChunkPos.rangeClosed(playerChunkPos, chunkRadius)
                .flatMap(chunkPos -> getAllInChunk(chunkPos, world))
                .filter(poi -> poi.getPos().distManhattan(player.blockPosition()) <= 50);
    }

    @Unique
    private static Stream<PoiRecord> getAllInChunk(ChunkPos chunkPos, ServerLevel world) {
        return IntStream.range(world.getMinSection(), world.getMaxSection())
                .boxed()
                .map(integer -> world.getPoiManager().getOrLoad(SectionPos.of(chunkPos, integer).asLong()))
                .filter(Optional::isPresent)
                .flatMap(optional -> getAll(optional.get()));
    }

    @Unique
    private static Stream<PoiRecord> getAll(PoiSection pointsOfInterestSet) {
        return pointsOfInterestSet.byType.values().stream()
                .flatMap(Set::stream);
    }
}
