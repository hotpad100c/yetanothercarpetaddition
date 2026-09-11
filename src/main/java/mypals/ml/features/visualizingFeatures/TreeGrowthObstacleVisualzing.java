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
import mypals.ml.utils.adapter.NBTDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//#if MC >= 260200
//$$ import java.util.Optional;
//$$ import net.minecraft.world.scores.TeamColor;
//#endif
//#if MC >= 260200
//$$ import net.minecraft.world.entity.EntityTypes;
//#endif

public class TreeGrowthObstacleVisualzing extends AbstractVisualizingManager<BlockPos, Display.BlockDisplay> {
    public static ConcurrentHashMap<BlockPos, Map.Entry<Display.BlockDisplay, Long>> visualizers = new ConcurrentHashMap<>();
    public static int SURVIVE_TIME = 40;
    public static int RANGE = 40;

    public void setVisualizer(Level world, BlockPos pos) {
        boolean playersNearBy = false;
        for (Player player : CarpetServer.minecraft_server.getPlayerList().players) {
            if (player.position().distanceTo(Vec3.atCenterOf(pos)) < RANGE) {
                playersNearBy = true;
                break;
            }
        }

        if (!playersNearBy) return;
        this.setVisualizer((ServerLevel) world, pos, Vec3.atCenterOf(pos), null);
    }

    private static void addMarkerToTeam(ServerLevel world, String teamName, Display.BlockDisplay marker) {
        Scoreboard scoreboard = world.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);

            //#if MC >= 260200
            //$$ team.setColor(Optional.of(TeamColor.RED));
            //#else
            team.setColor(ChatFormatting.RED);
            //#endif
        }
        String entityName = marker.getStringUUID();
        scoreboard.addPlayerToTeam(entityName, team);
    }


    @Override
    @SuppressWarnings("resource")
    public void updateVisualizer() {
        visualizers.forEach((pos, entry) -> {
            Display.BlockDisplay object = entry.getKey();
            long time = entry.getValue();
            if (time < object.level().getGameTime()) {
                removeVisualizer(pos);
                visualizers.remove(pos);
            }
        });
    }

    @Override
    protected void storeVisualizer(BlockPos key, Display.BlockDisplay entity) {
        visualizers.put(key, Map.entry(entity, getDeleteTick(SURVIVE_TIME, (ServerLevel) entity.level())));
    }

    @Override
    protected void updateVisualizerEntity(Display.BlockDisplay marker, Object data) {
    }

    @Override
    protected Display.BlockDisplay createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        //#if MC >= 260200
        //$$ Display.BlockDisplay entity = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, world);
        //#else
        Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
        //#endif
        entity.setNoGravity(true);

        CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
        //#if MC >= 260200
        //$$ nbt.put("block_state", NbtUtils.writeBlockState(Blocks.STAINED_GLASS.red().defaultBlockState()));
        //#else
        nbt.put("block_state", NbtUtils.writeBlockState(Blocks.RED_STAINED_GLASS.defaultBlockState()));
        //#endif
        float scale = 0.9f;
        nbt = EntityHelper.scaleEntity(nbt, scale);
        nbt.putInt("glow_color_override", 0xFF0000);
        //entity.readNbt(nbt);
        NBTDataManager.writeToEntity(entity, nbt);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setGlowingTag(true);
        entity.noPhysics = true;
        entity.setYRot(0);
        entity.setPosRaw(pos.x() - (scale / 2), pos.y() - (scale / 2), pos.z() - (scale / 2));
        entity.addTag(getVisualizerTag());
        entity.addTag("DoNotTick");
        if (!world.isClientSide()) {
            addMarkerToTeam(world, "treeGrowthObstacleVisualizerTeam", entity);
        }
        world.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Map.Entry<Display.BlockDisplay, Long> entry = visualizers.get(key);
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
    protected Display.BlockDisplay getVisualizer(BlockPos key) {
        return visualizers.get(key) == null ? null : visualizers.get(key).getKey();
    }

    @Override
    public String getVisualizerTag() {
        return "treeGrowthObstacleVisualize";
    }

}
