package mypals.ml.mixin.features.betterCommmand;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static mypals.ml.features.betterCommands.GamerulesDefaultValueSorter.gamerulesDefaultValues;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
    @WrapMethod(method = "register")
    private static <T> void register(CommandDispatcher<ServerCommandSource> dispatcher, Operation<Void> original) {
        final LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("gamerule")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    executeListCategories(context.getSource());

                    return 1;
                });
        GameRules.accept(new GameRules.Visitor() {
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                literalArgumentBuilder.then(
                        CommandManager.literal(key.getName())
                                .executes((context) -> GameRuleCommand.executeQuery(context.getSource(), key))
                                .then(
                                        type.argument("value")
                                                .executes((context) -> GameRuleCommand.executeSet(context, key))
                                )
                );
            }
        });
        literalArgumentBuilder.then(buildListByCategoryCommand());
        dispatcher.register(literalArgumentBuilder);
    }

    @Unique
    private static boolean isDefault(GameRules.Key<?> key, GameRules.Rule<?> rule) {
        return gamerulesDefaultValues.containsKey(key) && gamerulesDefaultValues.get(key).equals(rule.toString());
    }

    @Unique
    private static void executeListCategories(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Current Gamerule settings:").formatted(Formatting.BOLD), false);
        MutableText messageBuilder = Text.empty();
        boolean first = true;
        GameRules gameRules = source.getServer().getGameRules();
        for (GameRules.Key<?> key : gameRules.rules.keySet()) {
            if (!isDefault(key, gameRules.get(key))) {
                GameRules.Rule<?> rule = gameRules.get(key);
                MutableText ruleText = Text.literal("- " + key.getName() + ":").styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gamerule " + key.getName() + " "))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(rule.serialize())))
                        )
                        .append(getRuleValue(rule, key, true));
                source.sendFeedback(() -> ruleText, false);
            }
        }
        source.sendFeedback(() -> Text.literal("Minecraft: " + source.getServer().getVersion()).formatted(Formatting.GRAY), false);
        for (GameRules.Category category : GameRules.Category.values()) {
            String name = category.name().toLowerCase();

            if (!first) {
                messageBuilder.append(" ");
            } else {
                first = false;
            }

            MutableText clickable = Text.literal("[" + Text.translatable(category.getCategory()).getString() + "]")
                    .styled(style -> style
                            .withColor(Formatting.YELLOW)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamerule list " + name))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to view " + name)))
                    );

            messageBuilder.append(clickable);
        }

        source.sendFeedback(() -> Text.literal("Gamerules : \n").append(messageBuilder), false);

    }

    @Unique
    private static int executeListByCategory(CommandContext<ServerCommandSource> context) {
        String input = StringArgumentType.getString(context, "category").toUpperCase();
        ServerCommandSource source = context.getSource();
        GameRules.Category category;

        try {
            category = GameRules.Category.valueOf(input);
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal("X ->" + input));
            return 0;
        }

        GameRules gameRules = source.getServer().getGameRules();
        int count = 0;

        source.sendFeedback(() -> Text.literal("Gamerules in category: "
                + category.name().toLowerCase()).formatted(Formatting.BOLD), false);
        for (GameRules.Key<?> key : gameRules.rules.keySet()) {
            if (key.getCategory() == category) {
                GameRules.Rule<?> rule = gameRules.get(key);
                MutableText ruleText = Text.literal(key.getName() + ":").styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gamerule " + key.getName() + " "))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("/gamerule " + key.getName())))
                        )
                        .append(getRuleValue(rule, key, false));
                source.sendFeedback(() -> ruleText, false);
                count++;
            }
        }

        return count;
    }

    @Unique
    private static Text getRuleValue(GameRules.Rule<?> rule, GameRules.Key<?> key, boolean listAll) {
        Formatting color = Formatting.YELLOW;
        MutableText text = Text.empty();
        if (rule instanceof GameRules.BooleanRule booleanRule) {
            MutableText trueText = Text.literal("[true]").formatted(listAll ? Formatting.DARK_GREEN : Formatting.GRAY);
            MutableText falseText = Text.literal("[false]").formatted(listAll ? Formatting.DARK_GREEN : Formatting.GRAY);
            if (booleanRule.get()) {
                trueText = trueText.formatted(listAll ? Formatting.YELLOW : Formatting.GRAY).formatted(Formatting.UNDERLINE);
                if (!listAll) {
                    trueText = trueText.formatted(Formatting.BOLD);
                }
                falseText = falseText.styled(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/gamerule " + key.getName() + " false"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Set to false"))));

            } else {
                falseText = falseText.formatted(listAll ? Formatting.YELLOW : Formatting.GRAY).formatted(Formatting.UNDERLINE);
                if (!listAll) {
                    falseText = falseText.formatted(Formatting.BOLD);
                }
                trueText = trueText.styled(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/gamerule " + key.getName() + " true"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Set to true"))));

            }
            text.append(trueText).append(" ").append(falseText);
        }
        if (rule instanceof GameRules.IntRule intRule) {
            text.formatted(listAll ? Formatting.GRAY : Formatting.YELLOW);
            text.append(intRule.toString()).formatted(color);
        }
        return text;
    }

    @Unique
    private static LiteralArgumentBuilder<ServerCommandSource> buildListByCategoryCommand() {
        return CommandManager.literal("list")
                .then(CommandManager.argument("category", StringArgumentType.word())
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
