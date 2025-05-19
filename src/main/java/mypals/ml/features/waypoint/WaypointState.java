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

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WaypointState extends PersistentState {
    private static final String FILE_NAME = "waypoints";
    public static final Type<WaypointState> TYPE = new Type<>(
            WaypointState::new,
            (nbt, registryLookup) -> WaypointState.fromNbt(nbt),
            null
    );

    private final Map<String, BlockPos> waypoints = new HashMap<>();

    public WaypointState() {
    }


    public static WaypointState fromNbt(NbtCompound nbt) {
        WaypointState state = new WaypointState();
        NbtCompound waypointsNbt = nbt.getCompound("waypoints");

        for (String name : waypointsNbt.getKeys()) {
            int[] posArray = waypointsNbt.getIntArray(name);
            state.waypoints.put(name, new BlockPos(posArray[0], posArray[1], posArray[2]));
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound waypointsNbt = new NbtCompound();
        for (Map.Entry<String, BlockPos> entry : waypoints.entrySet()) {
            BlockPos pos = entry.getValue();
            waypointsNbt.putIntArray(entry.getKey(), new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }

        nbt.put("waypoints", waypointsNbt);
        return nbt;
    }

    public BlockPos getWaypoint(String name) {
        return waypoints.get(name);
    }

    public void setWaypoint(String name, BlockPos pos) {
        waypoints.put(name, pos);
        markDirty();
    }

    public boolean removeWaypoint(String name) {
        if (waypoints.remove(name) != null) {
            markDirty();
            return true;
        }
        return false;
    }

    public Set<String> getAllNames() {
        return waypoints.keySet();
    }

    public static WaypointState get(ServerWorld world) {
        return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(
                TYPE,
                FILE_NAME
        );
    }
}
