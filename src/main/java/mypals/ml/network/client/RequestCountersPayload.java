package mypals.ml.network.client;

import mypals.ml.network.PacketIDs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record RequestCountersPayload(String temp) implements CustomPayload {
    public static final Id<RequestCountersPayload> ID = new Id<>(PacketIDs.REQUEST_COUNTERS_DATA_ID);
    public static final PacketCodec<PacketByteBuf, RequestCountersPayload> CODEC = PacketCodec.of(
            (value, buf) ->
                    buf.writeString("hi")
            , buf -> new RequestCountersPayload(
                    buf.readString()
            ));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
