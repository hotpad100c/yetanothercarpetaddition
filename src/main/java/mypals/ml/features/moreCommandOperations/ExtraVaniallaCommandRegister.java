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

import carpet.utils.CommandHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;

import static mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandFeatureManager.*;
import static mypals.ml.features.moreCommandOperations.WorldEventMapper.WORLD_EVENT_MAP;

public class ExtraVaniallaCommandRegister {
    private static final SuggestionProvider<CommandSourceStack> WORLD_EVENT_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(WORLD_EVENT_MAP.keySet(), builder);

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("scheduleTick").requires(source -> CommandHelper.canUseCommand(source, 2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("block", BlockStateArgument.block(registryAccess))
                                .then(Commands.argument("time", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("priority", IntegerArgumentType.integer(-3, 3))
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();

                                                    BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                                    Block block = BlockStateArgument.getBlock(context, "block").getState().getBlock();
                                                    int time = IntegerArgumentType.getInteger(context, "time");
                                                    int priority = IntegerArgumentType.getInteger(context, "priority");
                                                    source.getLevel().scheduleTick(pos, block, time, TickPriority.byValue(priority));

                                                    source.sendSuccess(() -> Component.literal("ScheduleTick for [" + Component.translatable(block.getDescriptionId()).getString() + "] was added at [" + pos.getX() + "," +
                                                            pos.getY() + "," + pos.getZ() + "] with delay of [" + time + "]and priority[" + priority + "]."), true);

                                                    return Command.SINGLE_SUCCESS;
                                                }))))));
        dispatcher.register(Commands.literal("blockEvent").requires(source -> CommandHelper.canUseCommand(source, 2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("block", BlockStateArgument.block(registryAccess))
                                .then(Commands.argument("type", IntegerArgumentType.integer(0, 2))
                                        .then(Commands.argument("data", IntegerArgumentType.integer(0, 5))
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();

                                                    BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                                    Block block = BlockStateArgument.getBlock(context, "block").getState().getBlock();
                                                    int type = IntegerArgumentType.getInteger(context, "type");
                                                    int data = IntegerArgumentType.getInteger(context, "data");
                                                    addBlockEvent(source, pos, block, type, data);

                                                    return 1;
                                                }))))));
        dispatcher.register(Commands.literal("randomTick").requires(source -> CommandHelper.canUseCommand(source, 2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();

                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

                            addRandomTick(source, pos);

                            return 1;
                        })));
        dispatcher.register(
                Commands.literal("gameEvent").requires(source -> CommandHelper.canUseCommand(source, 2))
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("reason", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                BuiltInRegistries.GAME_EVENT.
                                                        //#if MC > 12101
                                                        listElements()
                                                        //#else
                                                        //$$holders()
                                                        //#endif
                                                        .map(entry -> entry.key().identifier().toString().replace("minecraft:", ""))
                                                        .collect(Collectors.toList()), builder
                                        ))
                                        .then(Commands.argument("entity", EntityArgument.entity())
                                                .then(Commands.argument("blockstate", BlockStateArgument.block(registryAccess))
                                                        .executes(context -> {
                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                            String reason = StringArgumentType.getString(context, "reason");
                                                            Entity entity = EntityArgument.getEntity(context, "entity");
                                                            BlockState blockState = BlockStateArgument.getBlock(context, "blockstate").getState();
                                                            return addGameEvent(context.getSource(), pos, reason, entity, blockState);
                                                        })
                                                )
                                        )
                                        .then(Commands.argument("entity", EntityArgument.entity())
                                                .executes(context -> {
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    String reason = StringArgumentType.getString(context, "reason");
                                                    Entity entity = EntityArgument.getEntity(context, "entity");
                                                    return addGameEvent(context.getSource(), pos, reason, entity, null);
                                                })
                                        ).then(Commands.argument("blockstate", BlockStateArgument.block(registryAccess))
                                                .executes(context -> {
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    String reason = StringArgumentType.getString(context, "reason");
                                                    BlockState blockState = BlockStateArgument.getBlock(context, "blockstate").getState();
                                                    return addGameEvent(context.getSource(), pos, reason, null, blockState);
                                                })
                                        )
                                        .executes(context -> {
                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                            String reason = StringArgumentType.getString(context, "reason");
                                            return addGameEvent(context.getSource(), pos, reason, null, null);
                                        })

                                )
                        )
        );
        dispatcher.register(Commands.literal("worldEvent").requires(source -> CommandHelper.canUseCommand(source, 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("event", StringArgumentType.string()).suggests(WORLD_EVENT_SUGGESTIONS)
                                        .then(Commands.argument("data", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();

                                                    Player player = EntityArgument.getPlayer(context, "player");
                                                    BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                                    String event = StringArgumentType.getString(context, "event");
                                                    int data = IntegerArgumentType.getInteger(context, "data");
                                                    addWorldEvent(source, pos, event, player, data);

                                                    return 1;
                                                }))))
                ).then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("event", StringArgumentType.string()).suggests(WORLD_EVENT_SUGGESTIONS)
                                .then(Commands.argument("data", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();

                                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                            String event = StringArgumentType.getString(context, "event");
                                            int data = IntegerArgumentType.getInteger(context, "data");
                                            addWorldEvent(source, pos, event, null, data);

                                            return 1;
                                        }))))
        );
    }
}
