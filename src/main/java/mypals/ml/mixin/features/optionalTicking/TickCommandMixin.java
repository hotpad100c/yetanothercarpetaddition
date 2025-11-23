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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.network.OptionalFreezePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TickCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.Final;

//#if MC < 12006
//$$ import net.minecraft.network.FriendlyByteBuf;
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//#endif

@Mixin(TickCommand.class)
public abstract class TickCommandMixin {
    @Unique
    private static LiteralArgumentBuilder<CommandSourceStack> freezeNode$YACA = null;


    @Unique
    private static final String[] PHASE_SUGGESTIONS = {
            "worldBorder", "weather", "time", "tileBlocks", "tileFluids", "tileTick",
            "raid", "chunkManager", "blockEvents", "dragonFight", "entityDespawn",
            "entities", "blockEntities", "spawners"
    };
    @Shadow
    @Final
    private static String DEFAULT_TICKRATE;

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
                    target = "Lnet/minecraft/commands/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 0
            )
    )
    private static LiteralArgumentBuilder<CommandSourceStack> storeFreezeNode(LiteralArgumentBuilder<CommandSourceStack> freezeNode) {
        freezeNode$YACA = freezeNode;
        return freezeNode;
    }


    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;",
                    remap = false
            )
    )
    private static LiteralArgumentBuilder<CommandSourceStack> enhanceFreezeAndUnfreeze(LiteralArgumentBuilder<CommandSourceStack> rootNode) {
        enhanceFreezeNode(rootNode);
        return rootNode;
    }

    @Unique
    private static void enhanceFreezeNode(LiteralArgumentBuilder<CommandSourceStack> rootNode) {
        rootNode.then(
                Commands.literal("freezePhase")
                        .executes(freezeNode$YACA.build().getCommand())
                        .then(
                                Commands.argument("phase", StringArgumentType.word())
                                        .suggests((context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(PHASE_SUGGESTIONS, suggestionsBuilder))
                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase")))
                        )
        );
    }


    @Unique
    private static int executePhaseFreeze(CommandSourceStack source, String phase) {
        boolean newFreezeState;

        switch (phase.toLowerCase()) {
            case "worldborder":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWorldBorder);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWorldBorder = newFreezeState;
                break;
            case "weather":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather = newFreezeState;
                break;
            case "time":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTime);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTime = newFreezeState;
                break;
            case "tileblocks":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks = newFreezeState;
                break;
            case "tilefluids":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids = newFreezeState;
                break;
            case "tiletick":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick = newFreezeState;
                break;
            case "raid":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingRaid);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingRaid = newFreezeState;
                break;
            case "chunkmanager":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingChunkManager);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingChunkManager = newFreezeState;
                break;
            case "blockevents":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEvents);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEvents = newFreezeState;
                break;
            case "dragonfight":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingDragonFight);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingDragonFight = newFreezeState;
                break;
            case "entitydespawn":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopCheckEntityDespawn);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopCheckEntityDespawn = newFreezeState;
                break;
            case "entities":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities = newFreezeState;
                break;
            case "blockentities":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEntities);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEntities = newFreezeState;
                break;
            case "spawners":
                newFreezeState = !(YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingSpawners);
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingSpawners = newFreezeState;
                break;
            default:
                throw new IllegalArgumentException("Unknown phase: " + phase);
        }

        // 发送状态给所有玩家
        source.getServer().getPlayerList().players.forEach(
                //#if MC >= 12006
                p -> ServerPlayNetworking.send(p, new OptionalFreezePayload(phase, newFreezeState))
                //#else
                //$$ p -> {
                //$$     FriendlyByteBuf buf = PacketByteBufs.create();
                //$$     buf.writeUtf(phase);
                //$$     buf.writeBoolean(newFreezeState);
                //$$     ServerPlayNetworking.send(p, OptionalFreezePayload.ID, buf);
                //$$ }
                //#endif
        );

        source.sendSuccess(() -> Component.translatable(newFreezeState ? "Froze" : "Unfroze").append(" [" + phase + "]"), true);
        return 1;
    }
}
