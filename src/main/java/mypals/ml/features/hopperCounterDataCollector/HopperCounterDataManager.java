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

package mypals.ml.features.hopperCounterDataCollector;

import carpet.CarpetServer;
import carpet.helpers.HopperCounter;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class HopperCounterDataManager {
    private static Map<String, HopperCounter> allCounters = new HashMap<>();
    private static CounterLogger counterLogger;
    private static int tickCounter = 0;

    public static void initCounterManager() throws IOException {
        allCounters.clear();
        for (DyeColor color : DyeColor.values()) {
            allCounters.put(color.getName(), HopperCounter.getCounter(color));
        }
        ServerWorld overworld = CarpetServer.minecraft_server.getOverworld();
        Path worldSavePath = overworld.getServer().getSavePath(WorldSavePath.ROOT).normalize();
        Path countersDir = worldSavePath.resolve("counters");
        String csvFilePath = countersDir.resolve("counters.csv").toString();
        if (counterLogger == null) {
            counterLogger = new CounterLogger(csvFilePath, allCounters.keySet().stream().toList());
        }
    }

    public static CounterLogger getCounterLogger() {
        return counterLogger;
    }

    public static void tick() {
        tickCounter++;
        if (tickCounter >= Integer.parseInt(YetAnotherCarpetAdditionRules.hopperCounterDataRecorder)) {
            tickCounter = 0;

            ServerWorld overworld = CarpetServer.minecraft_server.getOverworld();
            Path worldSavePath = overworld.getServer().getSavePath(WorldSavePath.ROOT).normalize();
            Path countersDir = worldSavePath.resolve("counters");
            String csvFilePath = countersDir.resolve("counters.csv").toString();

            try {

                if (counterLogger == null) {
                    counterLogger = new CounterLogger(csvFilePath, allCounters.keySet().stream().toList());
                }

                Map<String, String> counters = new HashMap<>();
                allCounters.forEach((name, counter) -> {
                    if (counter != null) {

                        String counterValue = counter.getTotalItems() + "^^^";
                        for (Text text : counter.format(CarpetServer.minecraft_server, false, false)) {
                            counterValue += text.getString() + "@@";
                        }
                        counters.put(name, counterValue);
                    }
                });

                counterLogger.logCounters(counters);
                YetAnotherCarpetAdditionServer.LOGGER.info("Logged counters to {}", csvFilePath);
            } catch (IOException e) {
                YetAnotherCarpetAdditionServer.LOGGER.error("Failed to log counters", e);
            }
        }
    }
}
