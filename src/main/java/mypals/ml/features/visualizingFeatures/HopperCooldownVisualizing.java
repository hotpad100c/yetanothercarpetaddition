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

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class HopperCooldownVisualizing extends AbstractVisualizingManager<BlockPos, DisplayEntity.TextDisplayEntity> {
    private static final Map<BlockPos, DisplayEntity.TextDisplayEntity> visualizers = new HashMap<>();

    @Override
    protected void storeVisualizer(BlockPos key, DisplayEntity.TextDisplayEntity entity) {
        visualizers.put(key, entity);
    }

    @Override
    protected void updateVisualizerEntity(DisplayEntity.TextDisplayEntity entity, Object data) {
        if (data instanceof Integer cooldown) {
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            String textJson = "{\"text\":\"" + "[" + cooldown + "]" + "\",\"color\":\"" + color + "\"}";
            nbt.remove("text");
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
        }
    }

    @Override
    protected DisplayEntity.TextDisplayEntity createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof Integer cooldown) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(getVisualizerTag());
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            String color = cooldown == 0 ? "green" : "red";
            nbt = configureCommonNbt(nbt);
            String textJson = "{\"text\":\"" + "[" + cooldown + "]" + "\",\"color\":\"" + color + "\"}";
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
            return entity;
        }
        return null;
    }

    @Override
    protected void removeVisualizerEntity(BlockPos key) {
        DisplayEntity.TextDisplayEntity entity = visualizers.get(key);
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
    protected DisplayEntity.TextDisplayEntity getVisualizer(BlockPos key) {
        return visualizers.get(key) == null ? null : visualizers.get(key);
    }

    @Override
    protected String getVisualizerTag() {
        return "hopperCooldownVisualizer";
    }
    

    @Override
    public void updateVisualizer() {
    }
}