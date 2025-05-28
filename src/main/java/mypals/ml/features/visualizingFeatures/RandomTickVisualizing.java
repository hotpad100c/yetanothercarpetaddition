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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static mypals.ml.features.visualizingFeatures.EntityHelper.mapSize;

public class RandomTickVisualizing extends AbstractVisualizingManager<BlockPos, DisplayEntity.BlockDisplayEntity> {
    public static ConcurrentHashMap<BlockPos, Map.Entry<DisplayEntity.BlockDisplayEntity, Long>> visualizers = new ConcurrentHashMap<>();
    public static int SURVIVE_TIME = 20;
    public static int RANGE = 20;

    public void setVisualizer(World world, BlockPos pos) {
        boolean playersNearBy = false;
        for (PlayerEntity player : CarpetServer.minecraft_server.getPlayerManager().players) {
            if (player.getPos().distanceTo(pos.toCenterPos()) < RANGE) {
                playersNearBy = true;
                break;
            }
        }

        if (!playersNearBy) return;
        this.setVisualizer((ServerWorld) world, pos, pos.toCenterPos(), null);
    }

    private static void addMarkerToTeam(ServerWorld world, String teamName, DisplayEntity.BlockDisplayEntity marker) {
        Scoreboard scoreboard = world.getScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);

            team.setColor(Formatting.RED);
        }
        String entityName = marker.getUuidAsString();
        scoreboard.addScoreHolderToTeam(entityName, team);
    }


    @Override
    public void updateVisualizer() {
        if (!CarpetServer.minecraft_server.getTickManager().shouldTick()) {
            return;
        }
        visualizers.forEach((pos, entry) -> {
            DisplayEntity.BlockDisplayEntity object = entry.getKey();
            long time = entry.getValue();
            if (time < object.getWorld().getTime()) {
                removeVisualizer(pos);
                visualizers.remove(pos);
            }
            NbtCompound nbt = entry.getKey().writeNbt(new NbtCompound());

            float scale = mapSize((int) (time - CarpetServer.minecraft_server.getOverworld().getTime()), SURVIVE_TIME, 0.9f);
            nbt = EntityHelper.scaleEntity(nbt, scale);
            entry.getKey().readNbt(nbt);
            entry.getKey().setPos(pos.toCenterPos().getX() - (scale / 2), pos.toCenterPos().getY() - (scale / 2), pos.toCenterPos().getZ() - (scale / 2));
        });
    }

    @Override
    protected void storeVisualizer(BlockPos key, DisplayEntity.BlockDisplayEntity entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerWorld) entity.getWorld())));
    }

    @Override
    protected void updateVisualizerEntity(DisplayEntity.BlockDisplayEntity marker, Object data) {
        NbtCompound nbt = marker.writeNbt(new NbtCompound());
        float scale = 0.9f;
        nbt = EntityHelper.scaleEntity(nbt, scale);
        marker.readNbt(nbt);
        BlockPos blockPos = BlockPos.ofFloored(marker.getPos());
        marker.setPos(blockPos.toCenterPos().getX() - (scale / 2), blockPos.toCenterPos().getY() - (scale / 2), blockPos.toCenterPos().getZ() - (scale / 2));
        marker.age = 0;
    }

    @Override
    protected DisplayEntity.BlockDisplayEntity createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
        entity.setNoGravity(true);
        NbtCompound nbt = entity.writeNbt(new NbtCompound());
        nbt.put("block_state", NbtHelper.fromBlockState(Blocks.RED_STAINED_GLASS.getDefaultState()));
        float scale = 0.9f;
        nbt = EntityHelper.scaleEntity(nbt, scale);
        nbt.putInt("glow_color_override", 0xFFAAAA);
        entity.readNbt(nbt);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setGlowing(true);
        entity.noClip = true;
        entity.setYaw(0);
        entity.setPos(pos.getX() - (scale / 2), pos.getY() - (scale / 2), pos.getZ() - (scale / 2));
        entity.addCommandTag(getVisualizerTag());
        entity.addCommandTag("DoNotTick");
        if (!world.isClient()) {
            addMarkerToTeam(world, "randomTickVisualizerTeam", entity);
        }
        world.spawnEntity(entity);
        return entity;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<DisplayEntity.BlockDisplayEntity, Long> entry = visualizers.get(key);
        if (entry != null) {
            entry.getKey().discard();
            visualizers.remove(key);
        }
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.clear();
    }


    @Override
    protected DisplayEntity.BlockDisplayEntity getVisualizer(BlockPos key) {
        return visualizers.get(key) == null ? null : visualizers.get(key).getKey();
    }

    @Override
    protected String getVisualizerTag() {
        return "randomTickVisualizer";
    }

}
