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

package mypals.ml.commands;

import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandRegister;
import mypals.ml.features.waypoint.WayPointCommand;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class YetAnotherCarpetAdditionCommands {
    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        ExtraVaniallaCommandRegister.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
        ItemCommand.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
        WayPointCommand.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
        SubscribeRuleCommand.registerCommand(serverCommandSourceCommandDispatcher, commandRegistryAccess);
        BindPlayerCommand.register(serverCommandSourceCommandDispatcher, commandRegistryAccess);
    }
}
