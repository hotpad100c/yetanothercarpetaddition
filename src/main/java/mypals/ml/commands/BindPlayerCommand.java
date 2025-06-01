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

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mypals.ml.features.fakePlayerControl.FakePlayerControlManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static mypals.ml.features.subscribeRules.RuleSubscribeManager.subscribeRule;

public class BindPlayerCommand {
    private static final SimpleCommandExceptionType NOT_REAL_PLAYER = new SimpleCommandExceptionType(
            Text.literal("The first argument must be a real player, not a fake player.")
    );
    private static final SimpleCommandExceptionType NOT_FAKE_PLAYER = new SimpleCommandExceptionType(
            Text.literal("The second argument must be a fake player.")
    );
    private static final SimpleCommandExceptionType PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
            Text.literal("Player not found.")
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                CommandManager.literal("bindToFake")
                        .requires(source -> CommandHelper.canUseCommand(source, CarpetSettings.commandPlayer))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("player2", EntityArgumentType.player())
                                        .executes(context -> execute(
                                                context,
                                                EntityArgumentType.getPlayer(context, "player"),
                                                EntityArgumentType.getPlayer(context, "player2")
                                        ))
                                )
                        )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context,
                               ServerPlayerEntity player, ServerPlayerEntity fakePlayer) throws CommandSyntaxException {
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
        context.getSource().sendFeedback(() -> Text.literal("Successfully " + (suc ? "bound " : "unbound ")
                + player.getNameForScoreboard() + " to " + fakePlayer.getNameForScoreboard()), true);
        return 1;
    }
}
