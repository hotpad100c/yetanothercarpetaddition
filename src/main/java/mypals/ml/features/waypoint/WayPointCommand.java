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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
//#if MC >= 12102
//$$ import net.minecraft.network.packet.s2c.play.PositionFlag;
//#endif

import java.util.HashSet;
import java.util.Set;

import static mypals.ml.features.waypoint.WaypointManager.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WayPointCommand {
    private static final SuggestionProvider<ServerCommandSource> WAYPOINT_SUGGESTIONS =
            (context, builder) -> {
                Set<String> names = new HashSet<>();
                for (Waypoint waypoint : waypoints){
                    names.add(waypoint.name);
                }
                return CommandSource.suggestMatching(names, builder);
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

                                    addWaypoint(name, pos, world.getRegistryKey().getValue().getPath());

                                    context.getSource().sendFeedback(() -> Text.literal("Saved waypoint '" + name + "' at " + pos), false);
                                    return 1;
                                })
                                .then(argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            String name = StringArgumentType.getString(context, "name");
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            ServerWorld world = player.getServerWorld();

                                            addWaypoint(name, pos ,world.getRegistryKey().getValue().getPath());

                                            context.getSource().sendFeedback(() -> Text.literal("Saved waypoint '" + name + "' at " + pos), false);
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word()).suggests(WAYPOINT_SUGGESTIONS)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    if (delWaypoint(name)) {
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
                                    if (player != null) {
                                        Waypoint waypoint = getWaypoint(name);
                                        if (waypoint != null) {
                                            BlockPos pos = waypoint.pos;
                                            ServerWorld world = player.getServer().getWorld(ServerWorld.OVERWORLD);
                                            switch (waypoint.dimension) {
                                                case "overworld" -> world = player.getServer().getWorld(ServerWorld.OVERWORLD);
                                                case "the_nether" -> world = player.getServer().getWorld(ServerWorld.NETHER);
                                                case "the_end" ->  world = player.getServer().getWorld(ServerWorld.END);
                                            }
                                            player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                                    //#if MC >= 12102
                                                    //$$ Set.of(),
                                                    //#endif
                                                    player.getYaw(), player.getPitch()
                                                    //#if MC >= 12102
                                                    //$$ , false
                                                    //#endif
                                            );
                                        }
                                    }else {
                                        context.getSource().sendError(Text.literal("Waypoint '" + name + "' does not exist."));
                                    }
                                    return 1;
                                })))
                .then(literal("list")
                        .executes(ctx -> {
                            if (waypoints.isEmpty()) {
                                ctx.getSource().sendFeedback(() -> Text.literal("No waypoints saved."), false);
                            } else {
                                ctx.getSource().sendFeedback(() -> Text.literal("Waypoints:").formatted(Formatting.GOLD), false);
                                for (Waypoint waypoint : waypoints) {
                                    Text clickable = Text.literal("• [" + waypoint.name + "]")
                                            .styled(style -> style
                                                    .withColor(Formatting.AQUA)
                                                    .withClickEvent(ClickEvent.runCommand( "/waypoint tp " + waypoint.name))
                                                    .withHoverEvent(HoverEvent.showText(( Text.literal("Click to teleport to " + waypoint.name))))
                                            );
                                    ctx.getSource().sendFeedback(() -> clickable, false);
                                }
                            }

                            return 1;
                        }))
        );
    }

}
