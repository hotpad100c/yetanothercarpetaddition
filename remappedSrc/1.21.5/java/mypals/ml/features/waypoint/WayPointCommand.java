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
import mypals.ml.features.waypoint.WaypointManager.Waypoint;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashSet;
import java.util.Set;

import static mypals.ml.features.waypoint.WaypointManager.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WayPointCommand {
    private static final SuggestionProvider<CommandSourceStack> WAYPOINT_SUGGESTIONS =
            (context, builder) -> {
                Set<String> names = new HashSet<>();
                for (Waypoint waypoint : waypoints) {
                    names.add(waypoint.name);
                }
                return SharedSuggestionProvider.suggest(names, builder);
            };

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("waypoint").requires(source -> source.hasPermission(2))
                .then(literal("save")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayer player = context.getSource().getPlayer();
                                    ServerLevel world = player.serverLevel();
                                    BlockPos pos = player.blockPosition();

                                    addWaypoint(name, pos, world.dimension().location().getPath());

                                    context.getSource().sendSuccess(() -> Component.literal("Saved waypoint '" + name + "' at " + pos), false);
                                    return 1;
                                })
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
                                            String name = StringArgumentType.getString(context, "name");
                                            ServerPlayer player = context.getSource().getPlayer();
                                            ServerLevel world = player.serverLevel();

                                            addWaypoint(name, pos, world.dimension().location().getPath());

                                            context.getSource().sendSuccess(() -> Component.literal("Saved waypoint '" + name + "' at " + pos), false);
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word()).suggests(WAYPOINT_SUGGESTIONS)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    if (delWaypoint(name)) {
                                        context.getSource().sendSuccess(() -> Component.literal("Removed waypoint '" + name + "'"), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("Waypoint '" + name + "' does not exist."));
                                    }
                                    return 1;
                                })))
                .then(literal("tp")
                        .then(argument("name", StringArgumentType.word()).suggests(WAYPOINT_SUGGESTIONS)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player != null) {
                                        Waypoint waypoint = getWaypoint(name);
                                        if (waypoint != null) {
                                            BlockPos pos = waypoint.pos;
                                            ServerLevel world = player.serverLevel().getServer().getLevel(ServerLevel.OVERWORLD);
                                            switch (waypoint.dimension) {
                                                case "overworld" ->
                                                        world = player.serverLevel().getServer().getLevel(ServerLevel.OVERWORLD);
                                                case "the_nether" ->
                                                        world = player.serverLevel().getServer().getLevel(ServerLevel.NETHER);
                                                case "the_end" -> world = player.serverLevel().getServer().getLevel(ServerLevel.END);
                                            }
                                            player.teleportTo(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                                                    //#if MC >= 12102
                                                    Set.of(),
                                                    //#endif
                                                    player.getYRot(), player.getXRot()
                                                    //#if MC >= 12102
                                                    , false
                                                    //#endif
                                            );
                                        }
                                    } else {
                                        context.getSource().sendFailure(Component.literal("Waypoint '" + name + "' does not exist."));
                                    }
                                    return 1;
                                })))
                .then(literal("list")
                        .executes(ctx -> {
                            if (waypoints.isEmpty()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("No waypoints saved."), false);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("Waypoints:").withStyle(ChatFormatting.GOLD), false);
                                for (Waypoint waypoint : waypoints) {
                                    Component clickable = Component.literal("• [" + waypoint.name + "]")
                                            .withStyle(style -> style
                                                    .withColor(ChatFormatting.AQUA)
                                                    .withClickEvent(ClickEvent.runCommand("/waypoint tp " + waypoint.name))
                                                    .withHoverEvent(HoverEvent.showText((Component.literal("Click to teleport to " + waypoint.name))))
                                            );
                                    ctx.getSource().sendSuccess(() -> clickable, false);
                                }
                            }

                            return 1;
                        }))
        );
    }

}
