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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static mypals.ml.features.subscribeRules.RuleSubscribeManager.subscribeRule;

public class SubscribeRuleCommand {
    private static SuggestionProvider<CommandSourceStack> suggestionProvider = (context, builder) -> {
        CarpetServer.settingsManager.getCarpetRules().stream()
                .map(rule -> rule.name())
                .forEach(builder::suggest);
        return builder.buildFuture();
    };

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("subscribeRule")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(suggestionProvider)
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, "name")
                        ))));
    }

    public static int execute(CommandSourceStack source, String ruleName) {
        subscribeRule(ruleName, source);
        return 1;
    }
}
