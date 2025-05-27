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

package mypals.ml.features.visualizingFeatures;

import carpet.CarpetServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static mypals.ml.features.visualizingFeatures.EntityHelper.clearWorldVisualizers;
import static mypals.ml.features.visualizingFeatures.EntityHelper.mapSize;

public class BlockUpdateVisualizing extends AbstractVisualizingManager<BlockPos, BlockUpdateVisualizing.BlockUpdateObject> {
    private static final ConcurrentHashMap<BlockPos, Map.Entry<BlockUpdateObject, Long>> visualizers = new ConcurrentHashMap<>();
    private static final int SURVIVE_TIME = 20;
    private static final int RANGE = 40;

    public enum UpdateType {
        NC("NCVisualizer", 0xff4f00, Formatting.RED, Blocks.RED_STAINED_GLASS.getDefaultState()),
        PP("PPVisualizer", 0x00ffff, Formatting.AQUA, Blocks.CYAN_STAINED_GLASS.getDefaultState()),
        CP("CPVisualizer", 0xffffed, Formatting.YELLOW, Blocks.YELLOW_STAINED_GLASS.getDefaultState());

        public final String tagName;
        public final int color;
        public final Formatting teamColor;
        public final BlockState defaultState;

        UpdateType(String tagName, int color, Formatting teamColor, BlockState defaultState) {
            this.tagName = tagName;
            this.color = color;
            this.teamColor = teamColor;
            this.defaultState = defaultState;
        }
    }

    public static class BlockUpdateObject {
        public final DisplayEntity.BlockDisplayEntity posMarker;
        public final UpdateType updateType;
        public final String tag;

        public BlockUpdateObject(ServerWorld world, BlockPos pos, UpdateType updateType, String tag) {
            this.updateType = updateType;
            this.tag = tag;
            this.posMarker = summonMarker(world, pos);
        }

        private DisplayEntity.BlockDisplayEntity summonMarker(ServerWorld world, BlockPos pos) {
            DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.9f;
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt.put("block_state", NbtHelper.fromBlockState(updateType.defaultState));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", updateType.color);
            entity.readNbt(nbt);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.noClip = true;
            entity.setYaw(0);
            entity.setPos(pos.toCenterPos().getX() - (scale / 2), pos.toCenterPos().getY() - (scale / 2), pos.toCenterPos().getZ() - (scale / 2));
            entity.addCommandTag(tag);
            entity.addCommandTag("DoNotTick");
            addMarkerToTeam(world, updateType.tagName, entity);
            world.spawnEntity(entity);
            return entity;
        }

        public void removeVisualizer() {
            if (posMarker != null && !posMarker.isRemoved()) {
                posMarker.discard();
            }
        }
    }

    @Override
    protected void storeVisualizer(BlockPos key, BlockUpdateObject entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerWorld) entity.posMarker.getWorld())));
    }

    @Override
    protected void updateVisualizerEntity(BlockUpdateObject marker, Object data) {
        if (marker.posMarker != null && !marker.posMarker.isRemoved() && !marker.posMarker.getWorld().isClient) {
            marker.posMarker.age = 0;
            NbtCompound nbt = marker.posMarker.writeNbt(new NbtCompound());
            float scale = 0.9f;
            nbt = EntityHelper.scaleEntity(nbt, scale);

            marker.posMarker.readNbt(nbt);
            BlockPos pos = BlockPos.ofFloored(marker.posMarker.getPos());
            marker.posMarker.setPos(pos.toCenterPos().getX() - (scale / 2), pos.toCenterPos().getY() - (scale / 2), pos.toCenterPos().getZ() - (scale / 2));
            visualizers.put(pos, Map.entry(marker, getDeleteTick(SURVIVE_TIME, (ServerWorld) marker.posMarker.getWorld())));
        }
    }

    @Override
    protected BlockUpdateObject createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof UpdateType updateType) {
            BlockPos blockPos = BlockPos.ofFloored(pos);
            return new BlockUpdateObject(world, blockPos, updateType, updateType.tagName);
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<BlockUpdateObject, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().removeVisualizer();
            visualizers.remove(key);
        }
    }

    @Override
    protected BlockUpdateObject getVisualizer(BlockPos key) {
        Map.Entry<BlockUpdateObject, Long> entry = visualizers.get(key);
        return entry == null ? null : entry.getKey();
    }

    @Override
    protected String getVisualizerTag() {
        return "";
    }

    @Override
    public void clearVisualizers(MinecraftServer server) {
            for (ServerWorld world : server.getWorlds()) {
                clearWorldVisualizers(world, "NCVisualizer");
                clearWorldVisualizers(world, "PPVisualizer");
                clearWorldVisualizers(world, "CPVisualizer");
            }
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.values().forEach(entry -> entry.getKey().removeVisualizer());
        visualizers.clear();
    }

    @Override
    public void updateVisualizer() {
        if (!CarpetServer.minecraft_server.getTickManager().shouldTick()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            BlockUpdateObject object = entry.getKey();
            long deleteTick = entry.getValue();
            if (deleteTick < object.posMarker.getWorld().getTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }
            NbtCompound nbt = object.posMarker.writeNbt(new NbtCompound());
            float scale = mapSize(SURVIVE_TIME - object.posMarker.age, SURVIVE_TIME, 0.9f);
            nbt = EntityHelper.scaleEntity(nbt, scale);
            object.posMarker.readNbt(nbt);
            object.posMarker.setPos(pos.toCenterPos().getX() - (scale / 2), pos.toCenterPos().getY() - (scale / 2), pos.toCenterPos().getZ() - (scale / 2));

        });
    }

    public void setVisualizer(ServerWorld world, BlockPos pos, UpdateType updateType) {
        boolean playersNearBy = false;
        for (PlayerEntity player : CarpetServer.minecraft_server.getPlayerManager().getPlayerList()) {
            if (player.getPos().distanceTo(pos.toCenterPos()) < RANGE) {
                playersNearBy = true;
                break;
            }
        }
        if (!playersNearBy) return;
        setVisualizer(world, pos, pos.toCenterPos(), updateType);
    }

    public void clearVisualizers(ServerCommandSource source, UpdateType updateType) {
        visualizers.entrySet().removeIf(entry -> {
            if (entry.getValue().getKey().updateType == updateType) {
                entry.getValue().getKey().removeVisualizer();
                return true;
            }
            return false;
        });
        EntityHelper.clearVisualizersInServer(source.getServer(), updateType.tagName);
    }

    public List<BlockPos> getVisualizersByType(UpdateType updateType) {
        return visualizers.entrySet().stream()
                .filter(entry -> entry.getValue().getKey().updateType == updateType)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static void addMarkerToTeam(ServerWorld world, String teamName, DisplayEntity.BlockDisplayEntity marker) {
        Scoreboard scoreboard = world.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
            UpdateType updateType = getUpdateTypeByTag(teamName);
            if (updateType != null) {
                team.setColor(updateType.teamColor);
            } else {
                team.setColor(Formatting.WHITE);
            }
        }
        String entityName = marker.getUuidAsString();
        scoreboard.addScoreHolderToTeam(entityName, team);
    }

    private static UpdateType getUpdateTypeByTag(String tagName) {
        for (UpdateType type : UpdateType.values()) {
            if (type.tagName.equals(tagName)) {
                return type;
            }
        }
        return null;
    }
}