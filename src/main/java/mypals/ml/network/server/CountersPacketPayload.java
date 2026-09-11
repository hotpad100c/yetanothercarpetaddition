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

import java.util.HashMap;
import java.util.Map;

//#if MC > 12004
import net.minecraft.network.codec.StreamCodec;
//#else
//$$ import net.minecraft.resources.ResourceLocation;
//#endif

public record CountersPacketPayload(Map<String, Map<String, String>> currentRecords) implements CustomPacketPayload {
    //#if MC >= 12006
    public static final Type<CountersPacketPayload> ID = new Type<>(PacketIDs.SYNC_COUNTERS_DATA_ID);
    public static final StreamCodec<FriendlyByteBuf, CountersPacketPayload> CODEC = StreamCodec.ofMember(CountersPacketPayload::write, CountersPacketPayload::new);
    //#else
    //$$ public static final ResourceLocation ID = PacketIDs.SYNC_COUNTERS_DATA_ID;
    //#endif

    // 26.3 removed FriendlyByteBuf#readMap/writeMap in favour of ByteBufCodecs; this is the same
    // wire format (varint size followed by the encoded key/value pairs) written out by hand.
    //#if MC >= 260300
    //$$ public CountersPacketPayload(FriendlyByteBuf buf) {
    //$$     this(readRecords(buf));
    //$$ }
    //$$ private static Map<String, Map<String, String>> readRecords(FriendlyByteBuf buf) {
    //$$     int size = buf.readVarInt();
    //$$     Map<String, Map<String, String>> records = new HashMap<>(size);
    //$$     for (int i = 0; i < size; i++) {
    //$$         String timestamp = buf.readUtf(); // Read timestamp
    //$$         int counterCount = buf.readVarInt();
    //$$         Map<String, String> counters = new HashMap<>(counterCount);
    //$$         for (int j = 0; j < counterCount; j++) {
    //$$             counters.put(buf.readUtf(), buf.readUtf()); // Read counter name and value
    //$$         }
    //$$         records.put(timestamp, counters);
    //$$     }
    //$$     return records;
    //$$ }
    //#else
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
    //#endif

    //#if MC < 12006
    //$$ @Override
    //#endif
    public void write(FriendlyByteBuf buf) {
        //#if MC >= 260300
        //$$ buf.writeVarInt(this.currentRecords().size());
        //$$ for (Map.Entry<String, Map<String, String>> entry : this.currentRecords().entrySet()) {
        //$$     buf.writeUtf(entry.getKey()); // Write timestamp
        //$$     buf.writeVarInt(entry.getValue().size());
        //$$     for (Map.Entry<String, String> counter : entry.getValue().entrySet()) {
        //$$         buf.writeUtf(counter.getKey()); // Write counter name
        //$$         buf.writeUtf(counter.getValue()); // Write counter value
        //$$     }
        //$$ }
        //#else
        buf.writeMap(
                this.currentRecords(), // Access currentRecords field
                FriendlyByteBuf::writeUtf, // Write timestamp
                (countersBuffer, counters) -> countersBuffer.writeMap(
                        counters,
                        FriendlyByteBuf::writeUtf, // Write counter name
                        FriendlyByteBuf::writeUtf  // Write counter value
                )
        );
        //#endif
    }

    //#if MC >= 12006
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    //#else
    //$$ @Override
    //$$ public ResourceLocation id() {
    //$$     return ID;
    //$$ }
    //#endif
}
