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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
//#if MC >= 260200
//$$ import net.minecraft.world.entity.EntityTypes;
//#endif

public class HopperCooldownVisualizing extends AbstractVisualizingManager<BlockPos, Display.TextDisplay> {
    private static final Map<BlockPos, Display.TextDisplay> visualizers = new HashMap<>();

    @Override
    protected void storeVisualizer(BlockPos key, Display.TextDisplay entity) {
        visualizers.put(key, entity);
    }

    @Override
    protected void updateVisualizerEntity(Display.TextDisplay entity, Object data) {
        if (entity.isRemoved()) {
            entity.discard();
            removeVisualizer(entity.blockPosition());
            return;
        }
        if (data instanceof Integer cooldown) {
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            String color = cooldown == 0 ? "green" : "red";
            //#if MC < 12105
            //$$ String textJson = "{\"text\":\"" + "[" + cooldown + "]" + "\",\"color\":\"" + color + "\"}";
            //$$ nbt.remove("text");
            //$$ nbt.putString("text", textJson);
            //#else
            HashMap<String, Tag> textNbt = new HashMap<>();
            textNbt.put("text", StringTag.valueOf("[" + cooldown + "]"));
            textNbt.put("color", StringTag.valueOf(color));
            CompoundTag textComponent = new CompoundTag(textNbt);
            nbt.put("text", textComponent);
            //#endif
            NBTDataManager.writeToEntity(entity, nbt);
        }
    }

    public static int RANGE = 50;

    public void setVisualizer(ServerLevel world, BlockPos key, Vec3 pos, Object data) {
        boolean playersNearBy = false;
        for (Player player : CarpetServer.minecraft_server.getPlayerList().players) {
            if (player.position().distanceTo(pos) < RANGE) {
                playersNearBy = true;
                break;
            }
        }

        if (!playersNearBy) return;
        super.setVisualizer((ServerLevel) world, key, pos, data);
    }

    @Override
    protected Display.TextDisplay createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof Integer cooldown) {
            //#if MC >= 260200
            //$$ Display.TextDisplay entity = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, world);
            //#else
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            //#endif
            entity.setInvisible(true);
            entity.setNoGravity(true);
            //#if MC >= 260300
            //$$ entity.setPermanentlyInvulnerable(true);
            //#else
            entity.setInvulnerable(true);
            //#endif
            entity.setPosRaw(pos.x(), pos.y(), pos.z());
            entity.addTag(getVisualizerTag());
            entity.addTag("DoNotTick");
            world.addFreshEntity(entity);
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            String color = cooldown == 0 ? "green" : "red";
            nbt = configureCommonNbt(nbt);
            String textJson = "{\"text\":\"" + "[" + cooldown + "]" + "\",\"color\":\"" + color + "\"}";
            nbt.putString("text", textJson);
            NBTDataManager.writeToEntity(entity, nbt);
            return entity;
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        Display.TextDisplay entity = visualizers.get(key);
        if (entity != null) {
            entity.discard();
            visualizers.remove(key);
        }
    }

    @Override
    protected void clearAllVisualizers() {
        visualizers.clear();
    }

    @Override
    protected Display.TextDisplay getVisualizer(BlockPos key) {
        return visualizers.get(key) == null ? null : visualizers.get(key);
    }

    @Override
    public String getVisualizerTag() {
        return "hopperCooldownVisualize";
    }


    @Override
    public void updateVisualizer() {
    }
}