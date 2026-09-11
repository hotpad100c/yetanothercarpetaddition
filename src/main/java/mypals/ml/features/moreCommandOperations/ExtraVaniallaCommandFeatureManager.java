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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//#if MC < 12006
//$$ import net.minecraft.resources.ResourceKey;
//$$ import net.minecraft.resources.ResourceLocation;
//#endif

import static mypals.ml.features.moreCommandOperations.WorldEventMapper.WORLD_EVENT_MAP;

public class ExtraVaniallaCommandFeatureManager {
    public static void addBlockEvent(CommandSourceStack source, BlockPos pos, Block block, int type, int data) {
        source.getLevel().blockEvent(pos, block, type, data);
        source.sendSuccess(() -> Component.literal("BlockEvent for [" + Component.translatable(block.getDescriptionId()).getString() + "] was emitted at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "] with type [" + type + "] and data[" + data + "]."), true);

    }

    public static int addGameEvent(CommandSourceStack source, Vec3 pos, String reason, @Nullable Entity entity, @Nullable BlockState blockState) {
        //#if MC > 12101
        Holder.Reference<GameEvent> event
        //#else
        //$$ GameEvent gameEvent
        //#endif
                = BuiltInRegistries.GAME_EVENT.get(
                //#if MC >= 12101
                Identifier.parse("minecraft:" + reason)
                //#elseif MC >= 12006
                //$$ new ResourceLocation("minecraft", reason)
                //#else
                //$$ ResourceKey.create(BuiltInRegistries.GAME_EVENT.key(), new ResourceLocation("minecraft", reason))
                //#endif
        )
                //#if MC > 12101
                .orElse(null)
                //#endif
                ;
        //#if MC<=12101
        //$$Holder<GameEvent> event = Holder.direct(gameEvent);
        //#endif
        if (event == null) {
            source.sendFailure(Component.literal("Unknown GameEvent: " + reason));
            return 0;
        }
        source.getLevel().gameEvent(
                //#if MC >= 12006
                event,
                //#else
                //$$ event.value(),
                //#endif
                pos, new GameEvent.Context(entity, blockState));
        source.sendSuccess(() -> Component.literal("GameEvent <" + reason + "> was emitted at [" + pos.x() + "," + pos.y() + "," + pos.z() + "]" +
                (entity == null ? " " : (" by entity <" + entity.getName() + ">")) + (blockState == null ? " " : (" with block [" +
                Component.translatable(blockState.getBlock().getDescriptionId()).getString() + "]"))), true);

        return 1;
    }

    public static Integer getEventId(String eventName) {
        return WORLD_EVENT_MAP.get(eventName);
    }

    public static void addRandomTick(CommandSourceStack source, BlockPos pos) {
        ServerLevel serverWorld = source.getLevel();
        serverWorld.getBlockState(pos).randomTick(serverWorld, pos, serverWorld.getRandom());
        YetAnotherCarpetAdditionServer.randomTickVisualizing.setVisualizer(serverWorld, pos, Vec3.atCenterOf(pos), "-");
        source.sendSuccess(() -> Component.literal("Simulated a RandomTick event at [" + pos.getX() + "," +
                pos.getY() + "," + pos.getZ() + "]."), true);


    }

    public static void addWorldEvent(CommandSourceStack source, BlockPos pos, String id, @Nullable Player player, int data) {
        int eventId = 1000;
        try {
            eventId = getEventId(id);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Unknown WorldEvent: " + id));
        }
        boolean global = eventId == 1023 || eventId == 1028 || eventId == 1038;
        if (global) {
            source.getLevel().globalLevelEvent(eventId, pos, data);
            source.sendSuccess(() -> Component.literal("<GLOBAL>WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        } else {
            source.getLevel().levelEvent(player, eventId, pos, data);
            source.sendSuccess(() -> Component.literal("WorldEvent <" + id + "> was emitted at [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() +
                    "]　with data of　[" + data + "]　." + (player == null ? " " : ("But will not notify the player:" + player.getName() + "..."))), true);

        }
    }
}
