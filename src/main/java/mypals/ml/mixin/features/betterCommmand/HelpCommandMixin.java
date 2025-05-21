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

package mypals.ml.mixin.features.betterCommmand;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.HelpCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(HelpCommand.class)
public class HelpCommandMixin {
    @Shadow
    @Final
    private static SimpleCommandExceptionType FAILED_EXCEPTION;
    private static List<String> names = new ArrayList<>();
    private static final SuggestionProvider<ServerCommandSource> commandNameSuggestionProvider = (context, builder) -> {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) {
            return Suggestions.empty();
        }
        Map<CommandNode<ServerCommandSource>, String> smartUsage = context.getSource()
                .getDispatcher()
                .getSmartUsage(context.getSource().getDispatcher().getRoot(), context.getSource());

        List<String> commandNames = new ArrayList<>();

        for (CommandNode<ServerCommandSource> node : smartUsage.keySet()) {
            commandNames.add(node.getName());
        }

        for (String commandName : commandNames) {
            builder.suggest(commandName);
        }

        return builder.buildFuture();
    };

    @WrapMethod(method = "register")
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, Operation<Void> original) {

        dispatcher.register(
                CommandManager.literal("help")
                        .executes(context -> {
                            if (!YetAnotherCarpetAdditionRules.commandEnhance) {
                                Map<CommandNode<ServerCommandSource>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());

                                for (String string : map.values()) {
                                    context.getSource().sendFeedback(() -> Text.literal("/" + string), false);
                                }

                                return map.size();
                            }
                            Map<CommandNode<ServerCommandSource>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());

                            for (CommandNode<ServerCommandSource> entry : map.keySet()) {
                                collectCommandNamesWithSource(entry);
                            }
                            MutableText feedback = Text.literal("");
                            feedback.append(Text.literal("Available commands:\n").formatted(Formatting.BOLD));
                            for (String name : names) {
                                feedback.append(Text.literal("[" + name + "] ")
                                        .styled(style -> style
                                                .withColor(Formatting.YELLOW)
                                                .withClickEvent(ClickEvent.runCommand("/help " + name))
                                                .withHoverEvent(HoverEvent.showText(Text.literal("Click to view " + name)))
                                        ));

                            }
                            context.getSource().sendFeedback(() -> feedback, false);
                            return map.size();
                        })
                        .then(
                                CommandManager.argument("command", StringArgumentType.greedyString())
                                        .suggests(commandNameSuggestionProvider)
                                        .executes(
                                                context -> {
                                                    ParseResults<ServerCommandSource> parseResults = dispatcher.parse(StringArgumentType.getString(context, "command"), context.getSource());
                                                    if (parseResults.getContext().getNodes().isEmpty()) {
                                                        throw FAILED_EXCEPTION.create();
                                                    } else {
                                                        Map<CommandNode<ServerCommandSource>, String> map = dispatcher.getSmartUsage(
                                                                Iterables.<ParsedCommandNode<ServerCommandSource>>getLast(parseResults.getContext().getNodes()).getNode(), context.getSource()
                                                        );
                                                        context.getSource().sendFeedback(() -> Text.literal("Commands in " + parseResults.getReader().getString()).formatted(Formatting.BOLD), false);
                                                        for (String string : map.values()) {
                                                            context.getSource().sendFeedback(() -> Text.literal("/" + parseResults.getReader().getString() + " ").formatted(Formatting.GREEN).append(Text.literal(string).formatted(Formatting.GRAY)), false);
                                                        }

                                                        return map.size();
                                                    }
                                                }
                                        )
                        )
        );
    }

    private static void collectCommandNamesWithSource(CommandNode<ServerCommandSource> node) {
        if (!names.contains(node.getName()))
            names.add(node.getName());
    }
}
