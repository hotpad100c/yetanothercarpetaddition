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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
//#if MC >= 12102
//$$ import net.minecraft.command.CommandRegistryAccess;
//#endif

import static mypals.ml.features.betterCommands.GamerulesDefaultValueSorter.gamerulesDefaultValues;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
    @WrapMethod(method = "register")
    private static <T> void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                     //#if MC >= 12102
                                     //$$ CommandRegistryAccess commandRegistryAccess,
                                     //#endif
                                     Operation<Void> original) {
        final LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("gamerule")
                .requires((source) -> source.hasPermission(2))
                .executes((context) -> {
                    executeListCategories(context.getSource());

                    return 1;
                });
        //#if MC < 12102
        GameRules
        //#else
        //$$ new GameRules(commandRegistryAccess.getEnabledFeatures())
        //#endif
            .visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                literalArgumentBuilder.then(
                        Commands.literal(key.getId())
                                .executes((context) -> GameRuleCommand.queryRule(context.getSource(), key))
                                .then(
                                        type.createArgument("value")
                                                .executes((context) -> GameRuleCommand.setRule(context, key))
                                )
                );
            }
        });
        literalArgumentBuilder.then(buildListByCategoryCommand());
        dispatcher.register(literalArgumentBuilder);
    }

    @Unique
    private static boolean isDefault(GameRules.Key<?> key, GameRules.Value<?> rule) {
        return gamerulesDefaultValues.containsKey(key) && gamerulesDefaultValues.get(key).equals(rule.toString());
    }

    @Unique
    private static void executeListCategories(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Current Gamerule settings:").withStyle(ChatFormatting.BOLD), false);
        MutableComponent messageBuilder = Component.empty();
        boolean first = true;
        GameRules gameRules = source.getServer().getGameRules();
        for (GameRules.Key<?> key : gameRules.rules.keySet()) {
            if (!isDefault(key, gameRules.getRule(key))) {
                GameRules.Value<?> rule = gameRules.getRule(key);
                MutableComponent ruleText = Component.literal("- " + key.getId() + ":").withStyle(style -> style
                                .withClickEvent(ClickEvent.suggestCommand("/gamerule " + key.getId() + " "))
                                .withHoverEvent(HoverEvent.showText(Component.translatable(rule.serialize())))
                        )
                        .append(getRuleValue(rule, key, true));
                source.sendSuccess(() -> ruleText, false);
            }
        }
        source.sendSuccess(() -> Component.literal("Minecraft: " + source.getServer().getServerVersion()).withStyle(ChatFormatting.GRAY), false);
        for (GameRules.Category category : GameRules.Category.values()) {
            String name = category.name().toLowerCase();

            if (!first) {
                messageBuilder.append(" ");
            } else {
                first = false;
            }

            MutableComponent clickable = Component.literal("[" + Component.translatable(category.getDescriptionId()).getString() + "]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(ClickEvent.runCommand("/gamerule list " + name))
                            .withHoverEvent(HoverEvent.showText(Component.literal("Click to view " + name)))
                    );

            messageBuilder.append(clickable);
        }

        source.sendSuccess(() -> Component.literal("Gamerules : \n").append(messageBuilder), false);

    }

    @Unique
    private static int executeListByCategory(CommandContext<CommandSourceStack> context) {
        String input = StringArgumentType.getString(context, "category").toUpperCase();
        CommandSourceStack source = context.getSource();
        GameRules.Category category;

        try {
            category = GameRules.Category.valueOf(input);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("X ->" + input));
            return 0;
        }

        GameRules gameRules = source.getServer().getGameRules();
        int count = 0;

        source.sendSuccess(() -> Component.literal("Gamerules in category: "
                + category.name().toLowerCase()).withStyle(ChatFormatting.BOLD), false);
        for (GameRules.Key<?> key : gameRules.rules.keySet()) {
            if (key.getCategory() == category) {
                GameRules.Value<?> rule = gameRules.getRule(key);
                MutableComponent ruleText = Component.literal(key.getId() + ":").withStyle(style -> style
                                .withClickEvent(ClickEvent.suggestCommand("/gamerule " + key.getId() + " "))
                                .withHoverEvent(HoverEvent.showText(Component.literal("/gamerule " + key.getId())))
                        )
                        .append(getRuleValue(rule, key, false));
                source.sendSuccess(() -> ruleText, false);
                count++;
            }
        }

        return count;
    }

    @Unique
    private static Component getRuleValue(GameRules.Value<?> rule, GameRules.Key<?> key, boolean listAll) {
        ChatFormatting color = ChatFormatting.YELLOW;
        MutableComponent text = Component.empty();
        if (rule instanceof GameRules.BooleanValue booleanRule) {
            MutableComponent trueText = Component.literal("[true]").withStyle(listAll ? ChatFormatting.DARK_GREEN : ChatFormatting.GRAY);
            MutableComponent falseText = Component.literal("[false]").withStyle(listAll ? ChatFormatting.DARK_GREEN : ChatFormatting.GRAY);
            if (booleanRule.get()) {
                trueText = trueText.withStyle(listAll ? ChatFormatting.YELLOW : ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE);
                if (!listAll) {
                    trueText = trueText.withStyle(ChatFormatting.BOLD);
                }
                falseText = falseText.withStyle(style ->
                        style.withClickEvent(ClickEvent.runCommand("/gamerule " + key.getId() + " false"))
                                .withHoverEvent(HoverEvent.showText(Component.literal("Set to false"))));

            } else {
                falseText = falseText.withStyle(listAll ? ChatFormatting.YELLOW : ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE);
                if (!listAll) {
                    falseText = falseText.withStyle(ChatFormatting.BOLD);
                }
                trueText = trueText.withStyle(style ->
                        style.withClickEvent(ClickEvent.runCommand("/gamerule " + key.getId() + " true"))
                                .withHoverEvent(HoverEvent.showText(Component.literal("Set to true"))));

            }
            text.append(trueText).append(" ").append(falseText);
        }
        if (rule instanceof GameRules.IntegerValue intRule) {
            text.withStyle(listAll ? ChatFormatting.GRAY : ChatFormatting.YELLOW);
            text.append(intRule.toString()).withStyle(color);
        }
        return text;
    }

    @Unique
    private static LiteralArgumentBuilder<CommandSourceStack> buildListByCategoryCommand() {
        return Commands.literal("list")
                .then(Commands.argument("category", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GameRules.Category cat : GameRules.Category.values()) {
                                builder.suggest(cat.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes((context) -> executeListByCategory(context))
                );
    }
}
