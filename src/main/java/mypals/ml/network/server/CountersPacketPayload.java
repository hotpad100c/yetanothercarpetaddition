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