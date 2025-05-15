package mypals.ml.network.client;

import mypals.ml.network.PacketIDs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record  RequestRulesPayload(String lang) implements CustomPayload {
    public static final Id<RequestRulesPayload> ID = new CustomPayload.Id<>(PacketIDs.REQUEST_RULES_ID);
    public static final PacketCodec<PacketByteBuf, RequestRulesPayload> CODEC = PacketCodec.of(
            (value, buf) ->
                    buf.writeString(value.lang)
            ,buf -> new RequestRulesPayload(
                    buf.readString()
            ));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
