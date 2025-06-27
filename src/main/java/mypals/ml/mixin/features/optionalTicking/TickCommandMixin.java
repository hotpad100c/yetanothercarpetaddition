/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025 Ryan100c and contributors
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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.Final;
//#if MC < 12006
//$$ import net.minecraft.network.PacketByteBuf;
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//#endif


import java.util.Arrays;

@Mixin(TickCommand.class)
public abstract class TickCommandMixin {
    @Unique
    private static LiteralArgumentBuilder<ServerCommandSource> freezeNode$YACA = null;
    @Unique
    private static LiteralArgumentBuilder<ServerCommandSource> unfreezeNode$YACA = null;
    @Unique
    private static final String[] PHASE_SUGGESTIONS = {
            "worldBorder", "weather", "time", "tileBlocks", "tileFluids", "tileTick",
            "raid", "chunkManager", "blockEvents", "dragonFight", "entityDespawn",
            "entities", "blockEntities", "spawners"
    };
    @Shadow
    @Final
    private static String DEFAULT_TICK_RATE_STRING;

    @ModifyExpressionValue(
            method = "register",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=freeze"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 0
            )
    )
    private static LiteralArgumentBuilder<ServerCommandSource> storeFreezeNode(LiteralArgumentBuilder<ServerCommandSource> freezeNode) {
        freezeNode$YACA = freezeNode;
        return freezeNode;
    }

    @ModifyExpressionValue(
            method = "register",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=unfreeze"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 0
            )
    )
    private static LiteralArgumentBuilder<ServerCommandSource> storeUnfreezeNode(LiteralArgumentBuilder<ServerCommandSource> unfreezeNode) {
        unfreezeNode$YACA = unfreezeNode;
        return unfreezeNode;
    }

    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/ComlmandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;"
            )
    )
    private static LiteralArgumentBuilder<ServerCommandSource> enhanceFreezeAndUnfreeze(LiteralArgumentBuilder<ServerCommandSource> rootNode) {
        enhanceFreezeNode(rootNode);
        enhanceUnfreezeNode(rootNode);
        return rootNode;
    }

    @Unique
    private static void enhanceFreezeNode(LiteralArgumentBuilder<ServerCommandSource> rootNode) {
        rootNode.then(
                CommandManager.literal("phase")
                        .executes(freezeNode$YACA.build().getCommand())
                        .then(
                                CommandManager.argument("phase", StringArgumentType.word())
                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(PHASE_SUGGESTIONS, suggestionsBuilder))
                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase"), true))
                        )
        );
    }
    @Unique
    private static void enhanceUnfreezeNode(LiteralArgumentBuilder<ServerCommandSource> rootNode) {
        rootNode.then(
                CommandManager.literal("phase")
                        .executes(unfreezeNode$YACA.build().getCommand())
                        .then(
                                CommandManager.argument("phase", StringArgumentType.word())
                                        .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(PHASE_SUGGESTIONS, suggestionsBuilder))
                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase"), false))
                        )
        );
    }
    @Unique
    private static String format(long nanos) {
        return String.format("%.1f", (float) nanos / (float) TimeHelper.MILLI_IN_NANOS);
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
                Text.translatable(freeze ? "Froze" : "Unfroze").append(" [" + phase + "]"), true);
        return 1;
    }
}
