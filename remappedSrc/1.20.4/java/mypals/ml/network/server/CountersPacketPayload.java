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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

public record CountersPacketPayload(Map<String, Map<String, String>> currentRecords) implements CustomPacketPayload {
    //#if MC >= 12006
    //$$ public static final Id<CountersPacketPayload> ID = new Id<>(PacketIDs.SYNC_COUNTERS_DATA_ID);
    //$$ public static final PacketCodec<PacketByteBuf, CountersPacketPayload> CODEC = PacketCodec.of(CountersPacketPayload::write, CountersPacketPayload::new);
    //#else
    public static final ResourceLocation ID = PacketIDs.SYNC_COUNTERS_DATA_ID;
    //#endif

    public CountersPacketPayload(FriendlyByteBuf buf) {
        this(buf.readMap(
                        FriendlyByteBuf::readUtf, // Read timestamp
                        countersBuffer -> countersBuffer.readMap(
                                FriendlyByteBuf::readUtf, // Read counter name
                                FriendlyByteBuf::readUtf  // Read counter value
                        )
                )
        );
    }

    //#if MC < 12006
    @Override
    //#endif
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(
                this.currentRecords(), // Access currentRecords field
                FriendlyByteBuf::writeUtf, // Write timestamp
                (countersBuffer, counters) -> countersBuffer.writeMap(
                        counters,
                        FriendlyByteBuf::writeUtf, // Write counter name
                        FriendlyByteBuf::writeUtf  // Write counter value
                )
        );
    }

    //#if MC >= 12006
    //$$ @Override
    //$$ public Id<? extends CustomPayload> getId() {
    //$$     return ID;
    //$$ }
    //#else
    @Override
    public ResourceLocation id() {
        return ID;
    }
    //#endif
}