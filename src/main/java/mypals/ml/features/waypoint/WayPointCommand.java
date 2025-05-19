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

package mypals.ml.features.waypoint;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.tick.TickPriority;

import java.util.Set;
import java.util.stream.Collectors;

import static mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandFeatureManager.*;
import static mypals.ml.features.moreCommandOperations.ExtraVaniallaCommandFeatureManager.addWorldEvent;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WayPointCommand {
    private static final SuggestionProvider<ServerCommandSource> WAYPOINT_SUGGESTIONS =
            (context, builder) -> {
                WaypointState state = WaypointState.get(context.getSource().getWorld());
                return CommandSource.suggestMatching(state.getAllNames(), builder);
            };

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("waypoint")
                .then(literal("save")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    ServerWorld world = player.getServerWorld();
                                    BlockPos pos = player.getBlockPos();

                                    WaypointState state = WaypointState.get(world);
                                    state.setWaypoint(name, pos);

                                    context.getSource().sendFeedback(() -> Text.literal("Saved waypoint '" + name + "' at " + pos), false);
                                    return 1;
                                })
                                .then(argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            String name = StringArgumentType.getString(context, "name");
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            ServerWorld world = player.getServerWorld();

                                            WaypointState state = WaypointState.get(world);
                                            state.setWaypoint(name, pos);

                                            context.getSource().sendFeedback(() -> Text.literal("Saved waypoint '" + name + "' at " + pos), false);
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word()).suggests(WAYPOINT_SUGGESTIONS)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    ServerWorld world = player.getServerWorld();

                                    WaypointState state = WaypointState.get(world);
                                    if (state.getWaypoint(name) != null) {
                                        state.removeWaypoint(name);
                                        context.getSource().sendFeedback(() -> Text.literal("Removed waypoint '" + name + "'"), false);
                                    } else {
                                        context.getSource().sendError(Text.literal("Waypoint '" + name + "' does not exist."));
                                    }
                                    return 1;
                                })))
                .then(literal("tp")
                        .then(argument("name", StringArgumentType.word()).suggests(WAYPOINT_SUGGESTIONS)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    ServerWorld world = player.getServerWorld();

                                    WaypointState state = WaypointState.get(world);
                                    BlockPos pos = state.getWaypoint(name);
                                    if (pos != null) {
                                        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYaw(), player.getPitch());
                                    } else {
                                        context.getSource().sendError(Text.literal("Waypoint '" + name + "' does not exist."));
                                    }
                                    return 1;
                                })))
                .then(literal("list")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            ServerWorld world = player.getServerWorld();
                            WaypointState state = WaypointState.get(world);
                            Set<String> names = state.getAllNames();

                            if (names.isEmpty()) {
                                ctx.getSource().sendFeedback(() -> Text.literal("No waypoints saved."), false);
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("Waypoints:").formatted(Formatting.GOLD), false);
                                for (String name : names) {
                                    Text clickable = Text.literal("• [" + name + "]")
                                            .styled(style -> style
                                                    .withColor(Formatting.AQUA)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/waypoint tp " + name))
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport to " + name)))
                                            );
                                    ctx.getSource().sendFeedback(() -> clickable, false);
                                }
                            }

                            return 1;
                        }))
        );
    }

}
