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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.tick.TickPriority;

import java.util.stream.Collectors;

import static mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandFeatureManager.*;
import static mypals.ml.features.moreCommandOperations.WorldEventMapper.WORLD_EVENT_MAP;

public class ExtraVaniallaCommandRegister {
    private static final SuggestionProvider<ServerCommandSource> WORLD_EVENT_SUGGESTIONS =
            (context, builder) -> CommandSource.suggestMatching(WORLD_EVENT_MAP.keySet(), builder);

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("scheduleTick")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .then(CommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                .then(CommandManager.argument("time", IntegerArgumentType.integer(0))
                                        .then(CommandManager.argument("priority", IntegerArgumentType.integer(-3, 3))
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();

                                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                                    Block block = BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock();
                                                    int time = IntegerArgumentType.getInteger(context, "time");
                                                    int priority = IntegerArgumentType.getInteger(context, "priority");
                                                    source.getWorld().scheduleBlockTick(pos, block,time, TickPriority.byIndex(priority));

                                                    source.sendFeedback(() -> Text.literal("ScheduleTick for [" + Text.translatable(block.getTranslationKey()).getString() + "] was added at [" + pos.getX() + "," +
                                                            pos.getY() + "," + pos.getZ() + "] with delay of [" + time + "]and priority[" + priority +"]."),true);

                                                    return Command.SINGLE_SUCCESS;
                                                }))))));
        dispatcher.register(CommandManager.literal("blockEvent")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .then(CommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                .then(CommandManager.argument("type", IntegerArgumentType.integer(0,2))
                                        .then(CommandManager.argument("data", IntegerArgumentType.integer(0, 5))
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();

                                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                                    Block block = BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock();
                                                    int type = IntegerArgumentType.getInteger(context, "type");
                                                    int data = IntegerArgumentType.getInteger(context, "data");
                                                    addBlockEvent(source,pos, block, type, data);

                                                    return 1;
                                                }))))));
        dispatcher.register(CommandManager.literal("randomTick")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();

                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");

                            addRandomTick(source,pos);

                            return 1;
                        })));
        dispatcher.register(
                CommandManager.literal("gameEvent")
                        .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                .then(CommandManager.argument("reason", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                Registries.GAME_EVENT.streamEntries()
                                                        .map(entry -> entry.registryKey().getValue().toString().replace("minecraft:",""))
                                                        .collect(Collectors.toList()), builder
                                        ))
                                        .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                                .then(CommandManager.argument("blockstate", BlockStateArgumentType.blockState(registryAccess))
                                                        .executes(context -> {
                                                            Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                                                            String reason = StringArgumentType.getString(context, "reason");
                                                            Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                            BlockState blockState = BlockStateArgumentType.getBlockState(context, "blockstate").getBlockState();
                                                            return addGameEvent(context.getSource(), pos, reason, entity, blockState);
                                                        })
                                                )
                                        )
                                        .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                                .executes(context -> {
                                                    Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                                                    String reason = StringArgumentType.getString(context, "reason");
                                                    Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                    return addGameEvent(context.getSource(), pos, reason, entity, null);
                                                })
                                        ).then(CommandManager.argument("blockstate", BlockStateArgumentType.blockState(registryAccess))
                                                .executes(context -> {
                                                    Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                                                    String reason = StringArgumentType.getString(context, "reason");
                                                    BlockState blockState = BlockStateArgumentType.getBlockState(context, "blockstate").getBlockState();
                                                    return addGameEvent(context.getSource(), pos, reason, null, blockState);
                                                })
                                        )
                                        .executes(context -> {
                                            Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
                                            String reason = StringArgumentType.getString(context, "reason");
                                            return addGameEvent(context.getSource(), pos, reason, null, null);
                                        })

                                )
                        )
        );
        dispatcher.register(CommandManager.literal("worldEvent")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("event", StringArgumentType.string()).suggests(WORLD_EVENT_SUGGESTIONS)
                                        .then(CommandManager.argument("data", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();

                                                    PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                                    String event = StringArgumentType.getString(context, "event");
                                                    int data = IntegerArgumentType.getInteger(context, "data");
                                                    addWorldEvent(source, pos, event,player, data);

                                                    return 1;
                                                }))))
                ).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .then(CommandManager.argument("event", StringArgumentType.string()).suggests(WORLD_EVENT_SUGGESTIONS)
                                .then(CommandManager.argument("data", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();

                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            String event = StringArgumentType.getString(context, "event");
                                            int data = IntegerArgumentType.getInteger(context, "data");
                                            addWorldEvent(source, pos, event,null, data);

                                            return 1;
                                        }))))
        );
    }
}
