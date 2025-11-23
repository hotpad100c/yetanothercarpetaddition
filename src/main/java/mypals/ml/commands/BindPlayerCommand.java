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

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mypals.ml.features.fakePlayerControl.FakePlayerControlManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BindPlayerCommand {
    private static final SimpleCommandExceptionType NOT_REAL_PLAYER = new SimpleCommandExceptionType(
            Component.literal("The first argument must be a real player, not a fake player.")
    );
    private static final SimpleCommandExceptionType NOT_FAKE_PLAYER = new SimpleCommandExceptionType(
            Component.literal("The second argument must be a fake player.")
    );
    private static final SimpleCommandExceptionType PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
            Component.literal("Player not found.")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                Commands.literal("bindToFake")
                        .requires(source -> CommandHelper.canUseCommand(source, CarpetSettings.commandPlayer))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("player2", EntityArgument.player())
                                        .executes(context -> execute(
                                                context,
                                                EntityArgument.getPlayer(context, "player"),
                                                EntityArgument.getPlayer(context, "player2")
                                        ))
                                )
                        )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context,
                               ServerPlayer player, ServerPlayer fakePlayer) throws CommandSyntaxException {
        if (player == null || fakePlayer == null) {
            throw PLAYER_NOT_FOUND.create();
        }
        if (player instanceof EntityPlayerMPFake) {
            throw NOT_REAL_PLAYER.create();
        }
        if (!(fakePlayer instanceof EntityPlayerMPFake)) {
            throw NOT_FAKE_PLAYER.create();
        }
        boolean suc = FakePlayerControlManager.tryBind(player, (EntityPlayerMPFake) fakePlayer);
        context.getSource().sendSuccess(() -> Component.literal("Successfully " + (suc ? "bound " : "unbound ")
                + player.getScoreboardName() + " to " + fakePlayer.getScoreboardName()), true);
        return 1;
    }
}
