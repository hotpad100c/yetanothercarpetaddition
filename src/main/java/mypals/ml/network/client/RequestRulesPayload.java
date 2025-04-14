package mypals.ml.network.client;

import mypals.ml.network.PacketIDs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record  RequestRulesPayload() implements CustomPayload {
    public static final Id<RequestRulesPayload> ID = new CustomPayload.Id<>(PacketIDs.REQUEST_RULES_ID);
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
