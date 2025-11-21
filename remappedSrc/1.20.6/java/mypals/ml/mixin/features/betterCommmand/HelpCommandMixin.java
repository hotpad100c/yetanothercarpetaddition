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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.HelpCommand;
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
    private static SimpleCommandExceptionType ERROR_FAILED;
    private static List<String> names = new ArrayList<>();
    private static final SuggestionProvider<CommandSourceStack> commandNameSuggestionProvider = (context, builder) -> {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false")) {
            return Suggestions.empty();
        }
        Map<CommandNode<CommandSourceStack>, String> smartUsage = context.getSource()
                .dispatcher()
                .getSmartUsage(context.getSource().dispatcher().getRoot(), context.getSource());

        List<String> commandNames = new ArrayList<>();

        for (CommandNode<CommandSourceStack> node : smartUsage.keySet()) {
            commandNames.add(node.getName());
        }

        for (String commandName : commandNames) {
            builder.suggest(commandName);
        }

        return builder.buildFuture();
    };

    @WrapMethod(method = "register")
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, Operation<Void> original) {

        dispatcher.register(
                Commands.literal("help")
                        .executes(context -> {
                            if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false")) {
                                Map<CommandNode<CommandSourceStack>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());

                                for (String string : map.values()) {
                                    context.getSource().sendSuccess(() -> Component.literal("/" + string), false);
                                }

                                return map.size();
                            }
                            Map<CommandNode<CommandSourceStack>, String> map = dispatcher.getSmartUsage(dispatcher.getRoot(), context.getSource());

                            for (CommandNode<CommandSourceStack> entry : map.keySet()) {
                                collectCommandNamesWithSource(entry);
                            }
                            MutableComponent feedback = Component.literal("");
                            feedback.append(Component.literal("Available commands:\n").withStyle(ChatFormatting.BOLD));
                            for (String name : names) {
                                feedback.append(Component.literal("[" + name + "] ")
                                        .withStyle(style -> style
                                                .withColor(ChatFormatting.YELLOW)
                                                .withClickEvent(ClickEvent.runCommand("/help " + name))
                                                .withHoverEvent(HoverEvent.showText(Component.literal("Click to view " + name)))
                                        ));

                            }
                            context.getSource().sendSuccess(() -> feedback, false);
                            return map.size();
                        })
                        .then(
                                Commands.argument("command", StringArgumentType.greedyString())
                                        .suggests(commandNameSuggestionProvider)
                                        .executes(
                                                context -> {
                                                    ParseResults<CommandSourceStack> parseResults = dispatcher.parse(StringArgumentType.getString(context, "command"), context.getSource());
                                                    if (parseResults.getContext().getNodes().isEmpty()) {
                                                        throw ERROR_FAILED.create();
                                                    } else {
                                                        Map<CommandNode<CommandSourceStack>, String> map = dispatcher.getSmartUsage(
                                                                Iterables.<ParsedCommandNode<CommandSourceStack>>getLast(parseResults.getContext().getNodes()).getNode(), context.getSource()
                                                        );
                                                        context.getSource().sendSuccess(() -> Component.literal("Commands in " + parseResults.getReader().getString()).withStyle(ChatFormatting.BOLD), false);
                                                        for (String string : map.values()) {
                                                            context.getSource().sendSuccess(() -> Component.literal("/" + parseResults.getReader().getString() + " ").withStyle(ChatFormatting.GREEN).append(Component.literal(string).withStyle(ChatFormatting.GRAY)), false);
                                                        }

                                                        return map.size();
                                                    }
                                                }
                                        )
                        )
        );
    }

    private static void collectCommandNamesWithSource(CommandNode<CommandSourceStack> node) {
        if (!names.contains(node.getName()))
            names.add(node.getName());
    }
}
