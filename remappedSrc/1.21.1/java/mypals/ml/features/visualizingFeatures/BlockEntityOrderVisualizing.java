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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
//#if MC >= 12105
//$$ import net.minecraft.nbt.NbtElement;
//$$ import net.minecraft.nbt.NbtString;
//#endif
import java.util.HashMap;
import java.util.Map;

public class BlockEntityOrderVisualizing extends AbstractVisualizingManager<BlockPos, Display.TextDisplay> {
    private static final Map<BlockPos, Display.TextDisplay> visualizers = new HashMap<>();
    public int globalOrder = 0;

    @Override
    protected void storeVisualizer(BlockPos key, Display.TextDisplay entity) {
        visualizers.put(key, entity);
    }

    @Override
    protected void updateVisualizerEntity(Display.TextDisplay entity, Object data) {
        if (entity.isRemoved()) {
            entity.discard();
            visualizers.remove(entity.blockPosition());
            return;
        }
        if (data instanceof Integer order) {
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            //#if MC < 12105
            String textJson = "{\"text\":\"" + "#" + order + "\",\"color\":\"" + "white" + "\"}";
            nbt.remove("text");
            nbt.putString("text", textJson);
            //#else
            //$$ HashMap<String, NbtElement> textNbt = new HashMap<>();
            //$$ textNbt.put("text", NbtString.of("#" + order));
            //$$ textNbt.put("color", NbtString.of("white"));
            //$$ NbtCompound textComponent = new NbtCompound(textNbt);
            //$$ nbt.put("text", textComponent);
            //#endif
            NBTDataManager.writeToEntity(entity, nbt);
        }
    }

    @Override
    protected Display.TextDisplay createVisualizerEntity(ServerLevel world, Vec3 pos, Object data) {
        if (data instanceof Integer order) {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPosRaw(pos.x(), pos.y(), pos.z());
            entity.addTag(getVisualizerTag());
            entity.addTag("DoNotTick");
            world.addFreshEntity(entity);
            CompoundTag nbt = NBTDataManager.readFromEntity(entity, new CompoundTag());
            nbt = configureCommonNbt(nbt);
            //#if MC < 12105
            String textJson = "{\"text\":\"" + "#" + order + "\",\"color\":\"" + "white" + "\"}";
            nbt.putString("text", textJson);
            //#else
            //$$ HashMap<String, NbtElement> textNbt = new HashMap<>();
            //$$ textNbt.put("text", NbtString.of("#" + order));
            //$$ textNbt.put("color", NbtString.of("white"));
            //$$ NbtCompound textComponent = new NbtCompound(textNbt);
            //$$ nbt.put("text", textComponent);
            //#endif
            NBTDataManager.writeToEntity(entity, nbt);
            return entity;
        }
        return null;
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
        return "blockEntityOrderVisualize";
    }


    @Override
    public void updateVisualizer() {
    }
}