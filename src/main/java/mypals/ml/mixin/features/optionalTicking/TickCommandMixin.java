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

package mypals.ml.mixin.features.optionalTicking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.network.OptionalFreezePayload;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TickCommand;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.TimeHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//#if MC < 12006
//$$ import net.minecraft.network.PacketByteBuf;
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//#endif

import java.util.Arrays;

@Mixin(TickCommand.class)
public abstract class TickCommandMixin {
    @Unique
    private static final String[] PHASE_SUGGESTIONS = {
            "worldBorder", "weather", "time", "tileBlocks", "tileFluids", "tileTick",
            "raid", "chunkManager", "blockEvents", "dragonFight", "entityDespawn",
            "entities", "blockEntities", "spawners"
    };
    @Shadow
    @Final
    private static String DEFAULT_TICK_RATE_STRING;

    @WrapMethod(method = "register")
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, Operation<Void> original) {
        dispatcher.register(
                CommandManager.literal("tick")
                        .requires(source -> source.hasPermissionLevel(3))
                        .then(
                                CommandManager.literal("query")
                                        .executes(context -> executeQuery(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("rate")
                                        .then(
                                                CommandManager.argument("rate", FloatArgumentType.floatArg(1.0F, 10000.0F))
                                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{DEFAULT_TICK_RATE_STRING}, suggestionsBuilder))
                                                        .executes(context -> executeRate(context.getSource(), FloatArgumentType.getFloat(context, "rate")))
                                        )
                        )
                        .then(
                                CommandManager.literal("step")
                                        .executes(context -> executeStep(context.getSource(), 1))
                                        .then(
                                                CommandManager.literal("stop")
                                                        .executes(context -> executeStopStep(context.getSource()))
                                        )
                                        .then(
                                                CommandManager.argument("time", TimeArgumentType.time(1))
                                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{"1t", "1s"}, suggestionsBuilder))
                                                        .executes(context -> executeStep(context.getSource(), IntegerArgumentType.getInteger(context, "time")))
                                        )
                        )
                        .then(
                                CommandManager.literal("sprint")
                                        .then(
                                                CommandManager.literal("stop")
                                                        .executes(context -> executeStopSprint(context.getSource()))
                                        )
                                        .then(
                                                CommandManager.argument("time", TimeArgumentType.time(1))
                                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{"60s", "1d", "3d"}, suggestionsBuilder))
                                                        .executes(context -> executeSprint(context.getSource(), IntegerArgumentType.getInteger(context, "time")))
                                        )
                        )
                        .then(
                                CommandManager.literal("freeze")
                                        .executes(context -> executeFreeze(context.getSource(), true))
                                        .then(
                                                CommandManager.argument("phase", StringArgumentType.word())
                                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(PHASE_SUGGESTIONS, suggestionsBuilder))
                                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase"), true))
                                        )
                        )
                        .then(
                                CommandManager.literal("unfreeze")
                                        .executes(context -> executeFreeze(context.getSource(), false))
                                        .then(
                                                CommandManager.argument("phase", StringArgumentType.word())
                                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(PHASE_SUGGESTIONS, suggestionsBuilder))
                                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase"), false))
                                        )
                        )
        );
    }

    @Unique
    private static int executeStep(ServerCommandSource source, int steps) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.step(steps);
        if (bl) {
            source.sendFeedback(() -> modifyFeedbackText(Text.translatable("commands.tick.step.success", steps), steps), true);

        } else {
            source.sendError(Text.translatable("commands.tick.step.fail"));
        }

        return 1;
    }

    @Unique
    private static int executeStopStep(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.stopStepping();
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.step.stop.success"), true);
            return 1;
        } else {
            source.sendError(Text.translatable("commands.tick.step.stop.fail"));
            return 0;
        }
    }

    @Unique
    private static int executeStopSprint(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.stopSprinting();
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.sprint.stop.success"), true);
            return 1;
        } else {
            source.sendError(Text.translatable("commands.tick.sprint.stop.fail"));
            return 0;
        }
    }

    @Unique
    private static String format(long nanos) {
        return String.format("%.1f", (float) nanos / (float) TimeHelper.MILLI_IN_NANOS);
    }

    @Unique
    private static int executeRate(ServerCommandSource source, float rate) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        serverTickManager.setTickRate(rate);
        String string = String.format("%.1f", rate);
        source.sendFeedback(() -> Text.translatable("commands.tick.rate.success", new Object[]{string}), true);
        return (int) rate;
    }

    @Unique
    private static int executeQuery(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        String string = format(source.getServer().getAverageNanosPerTick());
        float f = serverTickManager.getTickRate();
        String string2 = String.format("%.1f", f);
        if (serverTickManager.isSprinting()) {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.sprinting"), false);
            source.sendFeedback(() -> Text.translatable("commands.tick.query.rate.sprinting", new Object[]{string2, string}), false);
        } else {
            if (serverTickManager.isFrozen()) {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.frozen"), false);
            } else if (serverTickManager.getNanosPerTick() < source.getServer().getAverageNanosPerTick()) {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.lagging"), false);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.running"), false);
            }

            String string3 = format(serverTickManager.getNanosPerTick());
            source.sendFeedback(() -> Text.translatable("commands.tick.query.rate.running", new Object[]{string2, string, string3}), false);
        }

        long[] ls = Arrays.copyOf(source.getServer().getTickTimes(), source.getServer().getTickTimes().length);
        Arrays.sort(ls);
        String string4 = format(ls[ls.length / 2]);
        String string5 = format(ls[(int) ((double) ls.length * 0.95)]);
        String string6 = format(ls[(int) ((double) ls.length * 0.99)]);
        source.sendFeedback(() -> Text.translatable("commands.tick.query.percentiles", new Object[]{string4, string5, string6, ls.length}), false);
        return (int) f;
    }

    @Unique
    private static int executeSprint(ServerCommandSource source, int ticks) {
        boolean bl = source.getServer().getTickManager().startSprint(ticks);
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.sprint.stop.success"), true);
        }

        source.sendFeedback(() -> Text.translatable("commands.tick.status.sprinting"), true);
        return 1;
    }

    @Unique
    private static Text modifyFeedbackText(Text original, int steps) {
        MutableText modifiedText = original.copy();
        StepManager.step(steps);
        if (YetAnotherCarpetAdditionRules.enableTickStepCounter) {
            modifiedText.styled(style -> style.withHoverEvent(
                    HoverEvent.showText(
                            Text.literal(String.format(Text.translatable("TickStepCounter.stepped").getString(), StepManager.getStepped()))
                    )
            ));
        }
        return modifiedText;
    }

    @Unique
    private static int executeFreeze(ServerCommandSource source, boolean frozen) {
        StepManager.reset();
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        if (frozen) {
            if (serverTickManager.isSprinting()) {
                serverTickManager.stopSprinting();
            }

            if (serverTickManager.isStepping()) {
                serverTickManager.stopStepping();
            }
        }

        serverTickManager.setFrozen(frozen);
        if (frozen) {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.frozen"), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.running"), true);
        }

        return frozen ? 1 : 0;
    }

    @Unique
    private static int executePhaseFreeze(ServerCommandSource source, String phase, boolean freeze) {
        switch (phase.toLowerCase()) {
            case "worldborder":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWorldBorder = freeze;
                break;
            case "weather":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather = freeze;
                break;
            case "time":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTime = freeze;
                break;
            case "tileblocks":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks = freeze;
                break;
            case "tilefluids":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids = freeze;
                break;
            case "tiletick":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick = freeze;
                break;
            case "raid":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingRaid = freeze;
                break;
            case "chunkmanager":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingChunkManager = freeze;
                break;
            case "blockevents":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEvents = freeze;
                break;
            case "dragonfight":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingDragonFight = freeze;
                break;
            case "entitydespawn":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopCheckEntityDespawn = freeze;
                break;
            case "entities":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities = freeze;
                break;
            case "blockentities":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEntities = freeze;
                break;
            case "spawners":
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingSpawners = freeze;
                break;
            default:
                throw new IllegalArgumentException("Unknown phase: " + phase);
        }
        source.getServer().getPlayerManager().players.forEach(
                //#if MC >= 12006
                p -> ServerPlayNetworking.send(p, new OptionalFreezePayload(phase, freeze))
                //#else
                //$$ p -> {
                //$$     PacketByteBuf buf = PacketByteBufs.create();
                //$$     buf.writeString(phase);
                //$$     buf.writeBoolean(freeze);
                //$$     ServerPlayNetworking.send(p, OptionalFreezePayload.ID, buf);
                //$$ }
                //#endif

        );

        source.sendFeedback(() ->
                Text.translatable(freeze ? "Froze"
                        : "Unfreeze").append(
                        " [" + phase + "]"), true);
        return 1;
    }
}
