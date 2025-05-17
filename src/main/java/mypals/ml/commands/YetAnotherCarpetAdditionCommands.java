package mypals.ml.commands;

import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandRegister;
//import mypals.ml.features.waypoint.WayPointCommand;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class YetAnotherCarpetAdditionCommands {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        ExtraVaniallaCommandRegister.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
        ItemCommand.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
//        WayPointCommand.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
    }
}
