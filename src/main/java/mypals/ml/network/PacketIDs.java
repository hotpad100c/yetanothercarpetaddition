package mypals.ml.network;

import net.minecraft.util.Identifier;

import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_ID;

public class PacketIDs {
    public static final Identifier REQUEST_RULES_ID = Identifier.of(MOD_ID, "request_rules");
    public static final Identifier SYNC_RULES_ID = Identifier.of(MOD_ID, "sync_rules");
    public static final Identifier REQUEST_COUNTERS_DATA_ID = Identifier.of(MOD_ID, "request_counters");
    public static final Identifier SYNC_COUNTERS_DATA_ID = Identifier.of(MOD_ID, "sync_counters");
}
