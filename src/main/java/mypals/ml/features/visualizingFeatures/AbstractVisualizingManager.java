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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractVisualizingManager<T, E> {
    protected abstract void storeVisualizer(T key, E entity);

    protected abstract void updateVisualizerEntity(E marker, Object data);

    protected abstract E createVisualizerEntity(ServerWorld world, Vec3d pos, Object data);

    protected abstract void removeVisualizerEntity(T key);

    protected static long getDeleteTick(int duration, ServerWorld world) {
        return world.getTime() + duration;
    }


    public void setVisualizer(ServerWorld world, T key, Vec3d pos, Object data) {
        E marker = getVisualizer(key);
        if (marker != null) {
            //System.out.println("repeated marker added: " + pos);
            updateVisualizerEntity(marker, data);
        } else {
            //System.out.println("marker added: " + pos);
            marker = createVisualizerEntity(world, pos, data);
            storeVisualizer(key, marker);
        }
    }

    public void removeVisualizer(T key) {
        removeVisualizerEntity(key);
    }

    protected abstract void clearAllVisualizers();

    public void clearVisualizers(MinecraftServer server) {
        EntityHelper.clearVisualizersInServer(server, getVisualizerTag());
    }

    protected abstract E getVisualizer(T key);


    protected abstract String getVisualizerTag();


    protected NbtCompound configureCommonNbt(NbtCompound nbt) {
        nbt.putString("billboard", "center");
        nbt.putByte("see_through", (byte) 1);
        return nbt;
    }


    public abstract void updateVisualizer();
}
