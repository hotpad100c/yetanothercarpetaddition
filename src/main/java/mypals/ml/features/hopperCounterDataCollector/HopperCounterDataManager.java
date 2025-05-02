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
