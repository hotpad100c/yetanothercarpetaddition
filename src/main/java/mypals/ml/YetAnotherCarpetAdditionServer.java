package mypals.ml;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.api.settings.CarpetRule;
import carpet.logging.LoggerRegistry;
import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.commands.YetAnotherCarpetAdditionCommands;
import mypals.ml.features.hopperCounterDataCollector.HopperCounterDataManager;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.features.visualizingFeatures.*;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestCountersPayload;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.network.server.CountersPacketPayload;
import mypals.ml.network.server.RulesPacketPayload;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.translations.YetAnotherCarpetAdditionTranslations;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static mypals.ml.features.hopperCounterDataCollector.HopperCounterDataManager.initCounterManager;
import static mypals.ml.translations.YACALanguageUtil.getTranslation;

public class YetAnotherCarpetAdditionServer implements ModInitializer, CarpetExtension {
    public static final String MOD_NAME = "YetAnotherCarpetAddition";
    public static final String MOD_ID = MOD_NAME.toLowerCase();

    private static final List<AbstractVisualizingManager> allVisualizers = new ArrayList<>();
    public static GameEventVisualizing gameEventVisualizing = new GameEventVisualizing();
    public static HopperCooldownVisualizing hopperCooldownVisualizing = new HopperCooldownVisualizing();
    public static BlockEventVisualizing blockEventVisualizing = new BlockEventVisualizing();
    public static RandomTickVisualizing randomTickVisualizing = new RandomTickVisualizing();
    public static ScheduledTickVisualizing scheduledTickVisualizing = new ScheduledTickVisualizing();
    public static BlockUpdateVisualizing blockUpdateVisualizing = new BlockUpdateVisualizing();
    public static final String MOD_VERSION = "V1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ServerWorld serverWorld = null;

    static {
        allVisualizers.add(gameEventVisualizing);
        allVisualizers.add(hopperCooldownVisualizing);
        allVisualizers.add(randomTickVisualizing);
        allVisualizers.add(blockEventVisualizing);
        allVisualizers.add(scheduledTickVisualizing);
        allVisualizers.add(blockUpdateVisualizing);
    }

    @Override
    public String version() {
        return MOD_VERSION;
    }

    public String modID() {
        return MOD_ID;
    }

    public static void loadExtension() {
        CarpetServer.manageExtension(new YetAnotherCarpetAdditionServer());
    }

    public static boolean isInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onInitialize() {
        loadExtension();
        StepManager.reset();

        ServerWorldEvents.LOAD.register((server, world) -> {
            serverWorld = world;
            try {
                initCounterManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            if (!Objects.equals(YetAnotherCarpetAdditionRules.hopperCounterDataRecorder, "off")
                    && isInteger(YetAnotherCarpetAdditionRules.hopperCounterDataRecorder)
                    && CarpetSettings.hopperCounters)
                HopperCounterDataManager.tick();

            allVisualizers.forEach(visualizer -> visualizer.updateVisualizer());
        });
        PayloadTypeRegistry.playC2S().register(RequestRulesPayload.ID, RequestRulesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RulesPacketPayload.ID, RulesPacketPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        String lang = payload.lang();
                        RulesPacketPayload requestRulesPayload = new RulesPacketPayload(getRules(context.player().getServerWorld(), lang), getDefaults());
                        ServerPlayNetworking.send(context.player(), requestRulesPayload);
                    });
                });
        PayloadTypeRegistry.playC2S().register(RequestCountersPayload.ID, RequestCountersPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CountersPacketPayload.ID, CountersPacketPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestCountersPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        CountersPacketPayload requestRulesPayload = null;
                        try {
                            requestRulesPayload = new CountersPacketPayload(HopperCounterDataManager.getCounterLogger().readCounters());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ServerPlayNetworking.send(context.player(), requestRulesPayload);
                    });
                });
    }

    private Path getConfigFile() {
        return CarpetServer.minecraft_server.getSavePath(WorldSavePath.ROOT).resolve(CarpetServer.settingsManager.identifier() + ".conf");
    }

    private List<String> readSettingsFromConf(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = "";
            List<String> result = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("[\\r\\n]", "");
                String[] fields = line.split("\\s+", 2);
                if (fields.length > 1) {
                    if (result.isEmpty() && fields[0].startsWith("#") || fields[1].startsWith("#")) {
                        continue;
                    }
                    result.add(fields[0]);
                }
            }
            return result;
        } catch (IOException e) {
            CarpetSettings.LOG.error("Exception while loading Carpet rules from config", e);
            return new ArrayList<>();
        }
    }

    public String getDefaults() {
        StringBuilder defaults = new StringBuilder();
        readSettingsFromConf(getConfigFile()).forEach(c -> {
            defaults.append(c).append(";");
        });
        return defaults.toString();
    }

    public List<RuleData> getRules(ServerWorld serverWorld, String lang) {
        List<RuleData> rules = new ArrayList<>();

        CarpetServer.settingsManager.getCarpetRules().forEach(rule -> {
            if (rule instanceof CarpetRule<?>) {
                rules.add(
                        new RuleData(
                                getTranslation(lang, "carpet.rule." + rule.name() + ".name", rule.name()),
                                rule.type(),
                                rule.defaultValue().toString(),
                                rule.value().toString(),
                                getTranslation(lang, "carpet.rule." + rule.name() + ".desc", null),
                                rule.suggestions().stream().toList(),
                                rule.categories().stream().toList())
                );
            }
        });
        return rules;
    }

    @Override
    public void registerLoggers() {
        LoggerRegistry.registerLoggers();
    }

    @Override
    public void onGameStarted() {
        LOGGER.info(MOD_NAME + " loaded.");
        CarpetServer.settingsManager.parseSettingsClass(YetAnotherCarpetAdditionRules.class);

    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        allVisualizers.forEach(visualizer -> visualizer.clearVisualizers(server));
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandBuildContext) {
        YetAnotherCarpetAdditionCommands.register(dispatcher, commandBuildContext, CommandManager.RegistrationEnvironment.DEDICATED);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return YetAnotherCarpetAdditionTranslations.getTranslations(lang);
    }
}