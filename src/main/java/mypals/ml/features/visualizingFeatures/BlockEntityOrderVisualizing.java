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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
//#if MC >= 12105
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
//#endif
import java.util.HashMap;
import java.util.Map;

public class BlockEntityOrderVisualizing extends AbstractVisualizingManager<BlockPos, DisplayEntity.TextDisplayEntity> {
    private static final Map<BlockPos, DisplayEntity.TextDisplayEntity> visualizers = new HashMap<>();
    public int globalOrder = 0;

    @Override
    protected void storeVisualizer(BlockPos key, DisplayEntity.TextDisplayEntity entity) {
        visualizers.put(key, entity);
    }

    @Override
    protected void updateVisualizerEntity(DisplayEntity.TextDisplayEntity entity, Object data) {
        if (entity.isRemoved()) {
            entity.discard();
            visualizers.remove(entity.getBlockPos());
            return;
        }
        if (data instanceof Integer order) {
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
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
            entity.readNbt(nbt);
        }
    }

    @Override
    protected DisplayEntity.TextDisplayEntity createVisualizerEntity(ServerWorld world, Vec3d pos, Object data) {
        if (data instanceof Integer order) {
            DisplayEntity.TextDisplayEntity entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            entity.addCommandTag(getVisualizerTag());
            entity.addCommandTag("DoNotTick");
            world.spawnEntity(entity);
            NbtCompound nbt = entity.writeNbt(new NbtCompound());
            nbt = configureCommonNbt(nbt);
            String textJson = "{\"text\":\"" + "#" + order + "\",\"color\":\"" + "white" + "\"}";
            nbt.putString("text", textJson);
            entity.readNbt(nbt);
            return entity;
        }
        return null;
    }

    public static int RANGE = 50;

    public void setVisualizer(ServerWorld world, BlockPos key, Vec3d pos, Object data) {
        boolean playersNearBy = false;
        for (PlayerEntity player : CarpetServer.minecraft_server.getPlayerManager().players) {
            if (player.getPos().distanceTo(pos) < RANGE) {
                playersNearBy = true;
                break;
            }
        }

        if (!playersNearBy) return;
        super.setVisualizer((ServerWorld) world, key, pos, data);
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
        return "blockEntityOrderVisualizer";
    }


    @Override
    public void updateVisualizer() {
    }
}