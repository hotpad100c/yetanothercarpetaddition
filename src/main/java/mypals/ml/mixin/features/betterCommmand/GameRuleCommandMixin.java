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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.GameRuleCommand;
//#if MC >= 12111
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.server.permissions.Permissions;
//#else
//$$ import net.minecraft.world.level.GameRules;
//$$ import static mypals.ml.features.betterCommands.GamerulesDefaultValueSorter.gamerulesDefaultValues;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.stream.Stream;


@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
    @WrapMethod(method = "register")
    private static <T> void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                     //#if MC >= 12102
                                     CommandBuildContext commandRegistryAccess,
                                     //#endif
                                     Operation<Void> original) {
        final LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("gamerule")
                .requires((source) ->
                        //#if MC >= 12111
                        source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
                        //#else
                        //$$ source.hasPermission(2)
                        //#endif
                )
                .executes((context) -> {
                    executeListCategories(context.getSource());
                    return 1;
                });
        //#if MC < 12102
        //$$ GameRules
        //#else
        new GameRules(commandRegistryAccess.enabledFeatures())
        //#endif
            .visitGameRuleTypes(
                    //#if MC >= 12111
                    new GameRuleTypeVisitor() {
                        @Override
                        public <T> void visit(GameRule<T> gameRule) {
                            registerRule(literalArgumentBuilder, gameRule);
                        }
                    }
                    //#else
                    //$$ new GameRules.GameRuleTypeVisitor() {
                    //$$     public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    //$$         registerRule(literalArgumentBuilder, key, type);
                    //$$     }
                    //$$ }
                    //#endif
            );
        literalArgumentBuilder.then(buildListByCategoryCommand());
        dispatcher.register(literalArgumentBuilder);
    }

    @Unique
    //#if MC >= 12111
    private static <T> void registerRule(LiteralArgumentBuilder<CommandSourceStack> builder, GameRule<T> rule) {
        LiteralArgumentBuilder<CommandSourceStack> ruleIdBuilder  = Commands.literal(rule.id());
        LiteralArgumentBuilder<CommandSourceStack> ruleFullIdBuilder = Commands.literal(rule.getIdentifier().toString());
        builder.then(GameRuleCommand.buildRuleArguments(rule, ruleIdBuilder)).then(GameRuleCommand.buildRuleArguments(rule, ruleFullIdBuilder));
    }
    //#else
    //$$ private static <T extends GameRules.Value<T>> void  registerRule(LiteralArgumentBuilder<CommandSourceStack> builder, GameRules.Key<T> key, GameRules.Type<T> type) {
    //$$     builder.then(Commands.literal(key.getId())
    //$$             .executes((context) -> GameRuleCommand.queryRule(context.getSource(), key))
    //$$             .then(type.createArgument("value").executes((context) -> GameRuleCommand.setRule(context, key)))
    //$$     );
    //$$ }
    //#endif


    //#if MC < 12111
    //$$ @Unique
    //$$ private static boolean isDefault(GameRules.Key<?> key, GameRules.Value<?> rule) {
    //$$    return gamerulesDefaultValues.containsKey(key) && gamerulesDefaultValues.get(key).equals(rule.toString());
    //$$ }
    //#endif

    @Unique
    private static void executeListCategories(CommandSourceStack source) {
//      TODO: Test rule id or full id
        source.sendSuccess(() -> Component.literal("Current Gamerule settings:").withStyle(ChatFormatting.BOLD), false);
        MutableComponent messageBuilder = Component.empty();
        boolean first = true;
        //#if MC >= 12111
        GameRules gameRules = source.getLevel().getGameRules();
        gameRules.availableRules().forEach(key -> {
            String id = key.id();
            if (!gameRules.get(key).equals(key.defaultValue())) {
                Object value = gameRules.get(key);
                String valueString = gameRules.getAsString(key);
        //#else
        //$$ GameRules gameRules = source.getServer().getGameRules();
        //$$ for (GameRules.Key<?> key : gameRules.rules.keySet()) {
        //$$     String id = key.getId();
        //$$     if (!isDefault(key, gameRules.getRule(key))) {
        //$$         GameRules.Value<?> value = gameRules.getRule(key);
        //$$         String valueString = value.serialize();
        //#endif
                MutableComponent ruleText = Component.literal("- " + id + ":").withStyle(style -> style
                                .withClickEvent(ClickEvent.suggestCommand("/gamerule " + id + " "))
                                .withHoverEvent(HoverEvent.showText(Component.translatable(valueString)))
                        )
                        .append(getRuleValue(value, key, true));
                source.sendSuccess(() -> ruleText, false);
            }
        }
        //#if MC >= 12111
        );
        //#endif
        source.sendSuccess(() -> Component.literal("Minecraft: " + source.getServer().getServerVersion()).withStyle(ChatFormatting.GRAY), false);

        //#if MC >= 12111
        for (GameRuleCategory category : GameRuleCategory.SORT_ORDER) {
            String name = category.id().getPath();
            String id = "gamerule.category." + category.getDescriptionId().toLanguageKey(); //hacky
        //#else
        //$$ for (GameRules.Category category : GameRules.Category.values()) {
        //$$     String name = category.name().toLowerCase();
        //$$     String id = category.getDescriptionId();
        //#endif
            if (!first) {
                messageBuilder.append(" ");
            } else {
                first = false;
            }
            MutableComponent clickable = Component.literal("[" + Component.translatable(id).getString() + "]")
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
        String input = StringArgumentType.getString(context, "category");
        CommandSourceStack source = context.getSource();

        

        //#if MC >= 12111
        GameRuleCategory category;
        category = new GameRuleCategory(Identifier.withDefaultNamespace(input.toLowerCase()));
        if (!GameRuleCategory.SORT_ORDER.contains(category)){
        //#else
        //$$ GameRules.Category category;
        //$$ try {
        //$$     category = GameRules.Category.valueOf(input.toUpperCase());
        //$$ } catch (IllegalArgumentException e) {
        //#endif
            source.sendFailure(Component.literal("X ->" + input));
            return 0;
        }

        //#if MC >= 12111
        GameRules gameRules = source.getLevel().getGameRules();
        String categoryName = category.toString();
        //#else
        //$$ GameRules gameRules = source.getServer().getGameRules();
        //$$ String categoryName = category.name();
        //#endif

        source.sendSuccess(() -> Component.literal("Gamerules in category: "
                + categoryName.toLowerCase()).withStyle(ChatFormatting.BOLD), false);
        //#if MC >= 12111
        Stream<GameRule<?>> validRules = gameRules.availableRules().filter(key -> key.category().equals(category));
        //#else
        //$$ Stream<GameRules.Key<?>> validRules = gameRules.rules.keySet().stream().filter(key -> key.getCategory().equals(category));
        //#endif
        long count = validRules.peek(key -> {
            //#if MC >= 12111
            String id = key.id();
            Object value = gameRules.get(key);
            //#else
            //$$ String id = key.getId();
            //$$ GameRules.Value<?> value = gameRules.getRule(key);
            //#endif
            MutableComponent ruleText = Component.literal(id + ":").withStyle(style -> style
                            .withClickEvent(ClickEvent.suggestCommand("/gamerule " + id + " "))
                            .withHoverEvent(HoverEvent.showText(Component.literal("/gamerule " + id)))
                    )
                    .append(getRuleValue(value, key, false));
            source.sendSuccess(() -> ruleText, false);
        }).count();

        return (int)count;
    }

    @Unique
    //#if MC >= 12111
    private static Component getRuleValue(Object value, GameRule<?> key, boolean listAll) {
    //#else
    //$$ private static Component getRuleValue(GameRules.Value<?> rule, GameRules.Key<?> key, boolean listAll) {
    //#endif
        ChatFormatting color = ChatFormatting.YELLOW;
        MutableComponent text = Component.empty();
        //#if MC >= 12111
        String id = key.id();
        if (value instanceof Boolean bVal) {
            boolean boolValue = bVal;
        //#else
        //$$ String id = key.getId();
        //$$ if (rule instanceof GameRules.BooleanValue booleanRule) {
        //$$     boolean boolValue = booleanRule.get();
        //#endif
            MutableComponent trueText = Component.literal("[true]").withStyle(listAll ? ChatFormatting.DARK_GREEN : ChatFormatting.GRAY);
            MutableComponent falseText = Component.literal("[false]").withStyle(listAll ? ChatFormatting.DARK_GREEN : ChatFormatting.GRAY);
            if (boolValue) {
                trueText = trueText.withStyle(listAll ? ChatFormatting.YELLOW : ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE);
                if (!listAll) {
                    trueText = trueText.withStyle(ChatFormatting.BOLD);
                }
                falseText = falseText.withStyle(style ->
                        style.withClickEvent(ClickEvent.runCommand("/gamerule " + id + " false"))
                                .withHoverEvent(HoverEvent.showText(Component.literal("Set to false"))));

            } else {
                falseText = falseText.withStyle(listAll ? ChatFormatting.YELLOW : ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE);
                if (!listAll) {
                    falseText = falseText.withStyle(ChatFormatting.BOLD);
                }
                trueText = trueText.withStyle(style ->
                        style.withClickEvent(ClickEvent.runCommand("/gamerule " + id + " true"))
                                .withHoverEvent(HoverEvent.showText(Component.literal("Set to true"))));

            }
            text.append(trueText).append(" ").append(falseText);
        }
        //#if MC >= 12111
        if (value instanceof Integer) {
            text.withStyle(listAll ? ChatFormatting.GRAY : ChatFormatting.YELLOW);
            text.append(String.valueOf(value)).withStyle(color);
        }
        //#else
        //$$ if (rule instanceof GameRules.IntegerValue intRule) {
        //$$    text.withStyle(listAll ? ChatFormatting.GRAY : ChatFormatting.YELLOW);
        //$$    text.append(intRule.toString()).withStyle(color);
        //$$ }
        //#endif

        return text;
    }

    @Unique
    private static LiteralArgumentBuilder<CommandSourceStack> buildListByCategoryCommand() {
        return Commands.literal("list")
                .then(Commands.argument("category", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            //#if MC >= 12111
                            for (GameRuleCategory cat : GameRuleCategory.SORT_ORDER) {
                                builder.suggest(cat.id().getPath());
                            }
                            //#else
                            //$$ for (GameRules.Category cat : GameRules.Category.values()) {
                            //$$     builder.suggest(cat.name().toLowerCase());
                            //$$ }
                            //#endif
                            return builder.buildFuture();
                        })
                        .executes(GameRuleCommandMixin::executeListByCategory)
                );
    }
}
