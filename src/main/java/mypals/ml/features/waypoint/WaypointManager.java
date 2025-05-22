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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class WaypointManager {
    public static File configFile;
    public static Set<Waypoint> waypoints = new HashSet<>();

    public static class Waypoint{
        public String name;
        public BlockPos pos;
        public String dimension;
        public Waypoint(String name, String dimension, BlockPos pos) {
            this.name = name;
            this.dimension = dimension;
            this.pos = pos;
        }
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getSavePath(WorldSavePath.ROOT).toFile();
        File configDir = new File(worldDir, "YACA");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "waypoints.json");
        loadWaypoint();
    }

    private static void loadWaypoint() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<Set<Waypoint>>() {}.getType();
                waypoints = new Gson().fromJson(reader, type);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveWaypoint() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(waypoints, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Waypoint getWaypoint(String name) {
        for (Waypoint waypoint : waypoints){
            if(waypoint.name.equals(name)) return waypoint;
        }
        return null;
    }

    public static boolean addWaypoint(String name, BlockPos pos, String dimension) {
        for (Waypoint waypoint : waypoints){
            if(waypoint.name.equals(name)) return false;
        }
        waypoints.add(new Waypoint(name, dimension, pos));
        saveWaypoint();
        return true;
    }

    public static boolean delWaypoint(String name) {
        if (waypoints.removeIf(warning -> warning.name.equals(name))) {
            saveWaypoint();
            return true;
        } else {
            return false;
        }
    }

}
