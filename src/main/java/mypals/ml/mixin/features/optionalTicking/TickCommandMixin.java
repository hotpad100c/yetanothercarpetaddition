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
    

    @ModifyArg(
			method = "register",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;",
					remap = false
			)
	)
	private static LiteralArgumentBuilder<ServerCommandSource> enhanceFreezeAndUnfreeze(LiteralArgumentBuilder<ServerCommandSource> rootNode)
	{
            enhanceFreezeNode(rootNode);
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
                                        .executes(context -> executePhaseFreeze(context.getSource(), StringArgumentType.getString(context, "phase")))
                        )
        );
    }
    
	
    @Unique
    private static int executePhaseFreeze(ServerCommandSource source, String phase) {
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
    source.getServer().getPlayerManager().players.forEach(
        //#if MC >= 12006
        p -> ServerPlayNetworking.send(p, new OptionalFreezePayload(phase, newFreezeState))
        //#else
        //$$ p -> {
        //$$     PacketByteBuf buf = PacketByteBufs.create();
        //$$     buf.writeString(phase);
        //$$     buf.writeBoolean(newFreezeState);
        //$$     ServerPlayNetworking.send(p, OptionalFreezePayload.ID, buf);
        //$$ }
        //#endif
    );

    source.sendFeedback(() ->Text.translatable(newFreezeState ? "Froze" : "Unfroze").append(" [" + phase + "]"), true);
    return 1;
    }
}
