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

package mypals.ml.network.server;

import mypals.ml.network.PacketIDs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.Map;

public record CountersPacketPayload(Map<String, Map<String, String>> currentRecords) implements CustomPayload {
    public static final Id<CountersPacketPayload> ID = new Id<>(PacketIDs.SYNC_COUNTERS_DATA_ID);

    public static final PacketCodec<PacketByteBuf, CountersPacketPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeMap(
                        payload.currentRecords(), // Access currentRecords field
                        PacketByteBuf::writeString, // Write timestamp
                        (countersBuffer, counters) -> countersBuffer.writeMap(
                                counters,
                                PacketByteBuf::writeString, // Write counter name
                                PacketByteBuf::writeString  // Write counter value
                        )
                );
            },
            buf -> new CountersPacketPayload(
                    buf.readMap(
                            PacketByteBuf::readString, // Read timestamp
                            countersBuffer -> countersBuffer.readMap(
                                    PacketByteBuf::readString, // Read counter name
                                    PacketByteBuf::readString  // Read counter value
                            )
                    )
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}