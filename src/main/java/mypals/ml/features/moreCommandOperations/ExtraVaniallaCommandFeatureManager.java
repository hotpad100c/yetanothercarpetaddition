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

package mypals.ml.features.moreCommandOperations;

import mypals.ml.YetAnotherCarpetAdditionServer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
//#if MC < 12006
//$$ import net.minecraft.registry.RegistryKey;
//#endif

import static mypals.ml.features.moreCommandOperations.WorldEventMapper.WORLD_EVENT_MAP;

public class ExtraVaniallaCommandFeatureManager {
    public static void addBlockEvent(ServerCommandSource source, BlockPos pos, Block block, int type, int data) {
        source.getWorld().addSyncedBlockEvent(pos, block, type, data);
        source.sendFeedback(() -> Text.literal("BlockEvent for [" + Text.translatable(block.getTranslationKey()).getString() + "] was emitted at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "] with type [" + type + "] and data[" + data + "]."), true);

    }

    public static int addGameEvent(ServerCommandSource source, Vec3d pos, String reason, @Nullable Entity entity, @Nullable BlockState blockState) {
        RegistryEntry<GameEvent> event = Registries.GAME_EVENT.getEntry(
                //#if MC >= 12101
                Identifier.of("minecraft:" + reason)
                //#elseif MC >= 12006
                //$$ new Identifier("minecraft", reason)
                //#else
                //$$ RegistryKey.of(Registries.GAME_EVENT.getKey(), new Identifier("minecraft", reason))
                //#endif
        ).orElse(null);
        if (event == null) {
            source.sendError(Text.literal("Unknown GameEvent: " + reason));
            return 0;
        }
        source.getWorld().emitGameEvent(
                //#if MC >= 12006
                event,
                //#else
                //$$ event.value(),
                //#endif
                pos, new GameEvent.Emitter(entity, blockState));
        source.sendFeedback(() -> Text.literal("GameEvent <" + reason + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]" +
                (entity == null ? " " : (" by entity <" + entity.getName() + ">")) + (blockState == null ? " " : (" with block [" +
                Text.translatable(blockState.getBlock().getTranslationKey()).getString() + "]"))), true);

        return 1;
    }

    public static Integer getEventId(String eventName) {
        return WORLD_EVENT_MAP.get(eventName);
    }

    public static void addRandomTick(ServerCommandSource source, BlockPos pos) {
        ServerWorld serverWorld = source.getWorld();
        serverWorld.getBlockState(pos).randomTick(serverWorld, pos, serverWorld.getRandom());
        YetAnotherCarpetAdditionServer.randomTickVisualizing.setVisualizer(serverWorld, pos, pos.toCenterPos(), "-");
        source.sendFeedback(() -> Text.literal("Simulated a RandomTick event at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "]."), true);


    }

    public static void addWorldEvent(ServerCommandSource source, BlockPos pos, String id, @Nullable PlayerEntity player, int data) {
        int eventId = 1000;
        try {
            eventId = getEventId(id);
        } catch (Exception e) {
            source.sendError(Text.literal("Unknown WorldEvent: " + id));
        }
        boolean global = eventId == 1023 || eventId == 1028 || eventId == 1038;
        if (global) {
            source.getWorld().syncGlobalEvent(eventId, pos, data);
            source.sendFeedback(() -> Text.literal("<GLOBAL>WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        } else {
            source.getWorld().syncWorldEvent(player, eventId, pos, data);
            source.sendFeedback(() -> Text.literal("WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        }
    }
}
