package mypals.ml.network.server;

import mypals.ml.network.PacketIDs;
import mypals.ml.network.RuleData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record RulesPacketPayload(List<RuleData> rules, String defaults) implements CustomPayload {
    public static final CustomPayload.Id<RulesPacketPayload> ID = new CustomPayload.Id<>(PacketIDs.SYNC_RULES_ID);
    public static final PacketCodec<PacketByteBuf, RulesPacketPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeCollection(value.rules(), RuleData.CODEC);
                buf.writeString(value.defaults);
            },
            buf ->
                    new RulesPacketPayload(
                            buf.readList(RuleData.CODEC),
                            buf.readString()
                    )
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
