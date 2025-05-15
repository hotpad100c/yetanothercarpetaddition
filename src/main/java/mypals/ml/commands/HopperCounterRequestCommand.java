package mypals.ml.commands;

import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.network.client.RequestCountersPayload;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;

public class HopperCounterRequestCommand {
    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("counterGUI")
                .executes(context -> execute(
                        context.getSource()
                )));
    }

    public static int execute(FabricClientCommandSource source) {
        ClientPlayNetworking.send(new RequestCountersPayload("Server please give me some counter data owo"));
        return 1;
    }
}
