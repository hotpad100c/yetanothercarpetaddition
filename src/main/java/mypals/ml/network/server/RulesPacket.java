package mypals.ml.network.server;

import mypals.ml.network.PacketIDs;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestRulesPayload;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record RulesPacket(List<RuleData> rules) implements CustomPayload {
    public static final CustomPayload.Id<RequestRulesPayload> ID = new CustomPayload.Id<>(PacketIDs.REQUEST_RULES_ID);
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
