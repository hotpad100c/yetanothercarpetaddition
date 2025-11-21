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
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
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
        NC("NCVisualizer", 0xff4f00, ChatFormatting.RED, Blocks.RED_STAINED_GLASS.defaultBlockState()),
        PP("PPVisualizer", 0x00ffff, ChatFormatting.AQUA, Blocks.CYAN_STAINED_GLASS.defaultBlockState()),
        CP("CPVisualizer", 0xffffed, ChatFormatting.YELLOW, Blocks.YELLOW_STAINED_GLASS.defaultBlockState());

        public final String tagName;
        public final int color;
        public final ChatFormatting teamColor;
        public final BlockState defaultState;

        UpdateType(String tagName, int color, ChatFormatting teamColor, BlockState defaultState) {
            this.tagName = tagName;
            this.color = color;
            this.teamColor = teamColor;
            this.defaultState = defaultState;
        }
    }

    public static class BlockUpdateObject {
        public final Display.BlockDisplay posMarker;
        public final UpdateType updateType;
        public final String tag;

        public BlockUpdateObject(ServerLevel world, BlockPos pos, UpdateType updateType, String tag) {
            this.updateType = updateType;
            this.tag = tag;
            this.posMarker = summonMarker(world, pos);
        }

        private Display.BlockDisplay summonMarker(ServerLevel world, BlockPos pos) {
            Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
            float scale = 0.9f;
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt.put("block_state", NbtUtils.writeBlockState(updateType.defaultState));
            nbt = EntityHelper.scaleEntity(nbt, scale);
            nbt.putInt("glow_color_override", updateType.color);
            NBTDataManager.writeToEntity(entity, nbt);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGlowingTag(true);
            entity.noPhysics = true;
            entity.setYRot(0);
            entity.setPosRaw(pos.getCenter().x() - (scale / 2), pos.getCenter().y() - (scale / 2), pos.getCenter().z() - (scale / 2));
            entity.addTag(tag);
            entity.addTag("blockUpdateVisualize");
            entity.addTag("DoNotTick");
            addMarkerToTeam(world, updateType.tagName, entity);
            world.addFreshEntity(entity);
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
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerLevel) entity.posMarker.level())));
    }

    @Override
    protected void updateVisualizerEntity(BlockUpdateObject marker, Object data) {
        if (marker.posMarker != null && !marker.posMarker.isRemoved() && !marker.posMarker.level().isClientSide()) {
            marker.posMarker.tickCount = 0;
            CompoundTag nbt = NBTDataManager.readFromEntity(marker.posMarker, new CompoundTag());
            float scale = 0.9f;
            nbt = EntityHelper.scaleEntity(nbt, scale);

            NBTDataManager.writeToEntity(marker.posMarker, nbt);
            BlockPos pos = BlockPos.containing(marker.posMarker.position());
            marker.posMarker.setPosRaw(pos.getCenter().x() - (scale / 2), pos.getCenter().y() - (scale / 2), pos.getCenter().z() - (scale / 2));
            visualizers.put(pos, Map.entry(marker, getDeleteTick(SURVIVE_TIME, (ServerLevel) marker.posMarker.level())));
        }
    }

    @Override
    protected BlockUpdateObject createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof UpdateType updateType) {
            BlockPos blockPos = BlockPos.containing(pos);
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
    public String getVisualizerTag() {
        return "blockUpdateVisualize";
    }

    @Override
    public void clearVisualizers(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
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
        if (!CarpetServer.minecraft_server.tickRateManager().runsNormally()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            BlockUpdateObject object = entry.getKey();
            long deleteTick = entry.getValue();
            if (deleteTick < object.posMarker.level().getGameTime()) {
                object.removeVisualizer();
                visualizers.remove(pos);
            }

            CompoundTag nbt = NBTDataManager.readFromEntity(object.posMarker, new CompoundTag());
            float scale = mapSize((int) (deleteTick - CarpetServer.minecraft_server.overworld().getGameTime()), SURVIVE_TIME, 0.9f);
            nbt = EntityHelper.scaleEntity(nbt, scale);
            NBTDataManager.writeToEntity(object.posMarker, nbt);
            object.posMarker.setPosRaw(pos.getCenter().x() - (scale / 2), pos.getCenter().y() - (scale / 2), pos.getCenter().z() - (scale / 2));

        });
    }

    public void setVisualizer(ServerLevel world, BlockPos pos, UpdateType updateType) {
        boolean playersNearBy = false;
        for (Player player : CarpetServer.minecraft_server.getPlayerList().getPlayers()) {
            if (player.position().distanceTo(pos.getCenter()) < RANGE) {
                playersNearBy = true;
                break;
            }
        }
        if (!playersNearBy) return;
        setVisualizer(world, pos, pos.getCenter(), updateType);
    }

    public void clearVisualizers(CommandSourceStack source, UpdateType updateType) {
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

    private static void addMarkerToTeam(ServerLevel world, String teamName, Display.BlockDisplay marker) {
        Scoreboard scoreboard = world.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            UpdateType updateType = getUpdateTypeByTag(teamName);
            if (updateType != null) {
                team.setColor(updateType.teamColor);
            } else {
                team.setColor(ChatFormatting.WHITE);
            }
        }
        String entityName = marker.getStringUUID();
        scoreboard.addPlayerToTeam(entityName, team);
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