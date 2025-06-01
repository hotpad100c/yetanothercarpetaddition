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

package mypals.ml;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.api.settings.CarpetRule;
import carpet.logging.LoggerRegistry;
import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.CommandDispatcher;
import mypals.ml.commands.YetAnotherCarpetAdditionCommands;
import mypals.ml.features.GridWorldGen.GridWorldGenerator;
import mypals.ml.features.fakePlayerControl.FakePlayerControlManager;
import mypals.ml.features.hopperCounterDataCollector.HopperCounterDataManager;
import mypals.ml.features.selectiveFreeze.SelectiveFreezeManager;
import mypals.ml.features.subscribeRules.RuleSubscribeManager;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.features.visualizingFeatures.BlockEventVisualizing;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.features.visualizingFeatures.GameEventVisualizing;
import mypals.ml.features.visualizingFeatures.RandomTickVisualizing;
import mypals.ml.features.visualizingFeatures.*;
import mypals.ml.features.waypoint.WaypointManager;
import mypals.ml.network.OptionalFreezePayload;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestCountersPayload;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.network.server.CountersPacketPayload;
import mypals.ml.network.server.RulesPacketPayload;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.translations.YetAnotherCarpetAdditionTranslations;
import mypals.ml.utils.POIManage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
//#if MC >= 12006
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//#endif
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
//#if MC < 12006
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//$$ import net.minecraft.network.PacketByteBuf;
//#endif

import static mypals.ml.features.hopperCounterDataCollector.HopperCounterDataManager.initCounterManager;
import static mypals.ml.translations.YACALanguageUtil.getTranslation;

public class YetAnotherCarpetAdditionServer implements ModInitializer, CarpetExtension {
    public static final String MOD_NAME = "YetAnotherCarpetAddition";
    public static final String MOD_ID = MOD_NAME.toLowerCase();
    public static SelectiveFreezeManager selectiveFreezeManager = new SelectiveFreezeManager();
    private static final List<AbstractVisualizingManager> allVisualizers = new ArrayList<>();
    public static GameEventVisualizing gameEventVisualizing = new GameEventVisualizing();
    public static HopperCooldownVisualizing hopperCooldownVisualizing = new HopperCooldownVisualizing();
    public static BlockEventVisualizing blockEventVisualizing = new BlockEventVisualizing();
    public static RandomTickVisualizing randomTickVisualizing = new RandomTickVisualizing();
    public static ScheduledTickVisualizing scheduledTickVisualizing = new ScheduledTickVisualizing();
    public static BlockUpdateVisualizing blockUpdateVisualizing = new BlockUpdateVisualizing();
    public static BlockEntityOrderVisualizing blockEntityOrderVisualizing = new BlockEntityOrderVisualizing();
    public static POIVisualizing poiVisualizing = new POIVisualizing();
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
        allVisualizers.add(blockEntityOrderVisualizing);
        allVisualizers.add(poiVisualizing);
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
        GridWorldGenerator.init();
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            WaypointManager.init(server);
            RuleSubscribeManager.init(server);

        });
        ServerWorldEvents.LOAD.register((server, world) -> {
            serverWorld = world;
            try {
                initCounterManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ServerTickEvents.END_WORLD_TICK.register((world) -> {
            FakePlayerControlManager.tickBinds(world);
            if (YetAnotherCarpetAdditionRules.POIVisualize) {
                world.getServer().getPlayerManager().players.forEach(
                        player -> {
                            POIManage.getPOIsWithinRange(player, world,
                                            POIVisualizing.RANGE)
                                    .forEach(poi -> {
                                        PointOfInterestType type = poi.getType().value();
                                        Vec3d pos = poi.getPos().toCenterPos();
                                        YetAnotherCarpetAdditionServer.poiVisualizing.setVisualizer(
                                                player.getServerWorld(),
                                                poi.getPos(),
                                                pos,
                                                poi
                                        );
                                    });
                        });
            }

            if (!Objects.equals(YetAnotherCarpetAdditionRules.hopperCounterDataRecorder, "off")
                    && isInteger(YetAnotherCarpetAdditionRules.hopperCounterDataRecorder)
                    && CarpetSettings.hopperCounters)
                HopperCounterDataManager.tick();

            allVisualizers.forEach(AbstractVisualizingManager::updateVisualizer);
        });
        //#if MC >= 12006
        PayloadTypeRegistry.playS2C().register(OptionalFreezePayload.ID, OptionalFreezePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestRulesPayload.ID, RequestRulesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RulesPacketPayload.ID, RulesPacketPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestCountersPayload.ID, RequestCountersPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CountersPacketPayload.ID, CountersPacketPayload.CODEC);
        //#endif
        ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.server().execute(() -> {
                    String lang = payload.lang();
                    ServerPlayerEntity player = context.player();
                    //#else
                    //$$ (server, player, handler, buf, responseSender) -> server.execute(() -> {
                    //$$ String lang = buf.readString();
                    //#endif
                    RulesPacketPayload rulesPacketPayload = new RulesPacketPayload(getRules(player.getServerWorld(), lang), getDefaults());
                    //#if MC >= 12006
                    ServerPlayNetworking.send(player, rulesPacketPayload);
                    //#else
                    //$$ PacketByteBuf data = PacketByteBufs.create();
                    //$$ data.writeCollection(
                    //$$         rulesPacketPayload.rules(),
                    //$$         (rulesBuffer, rule) -> rule.write(rulesBuffer)
                    //$$ );
                    //$$ data.writeString(rulesPacketPayload.defaults());
                    //$$ ServerPlayNetworking.send(player, RulesPacketPayload.ID, data);
                    //#endif
                })
        );
        ServerPlayNetworking.registerGlobalReceiver(RequestCountersPayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    //#else
                    //$$ (server, player, handler, buf, responseSender) -> server.execute(() -> {
                    //#endif
                    CountersPacketPayload countersPacketPayload = null;
                    try {
                        countersPacketPayload = new CountersPacketPayload(HopperCounterDataManager.getCounterLogger().readCounters());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //#if MC >= 12006
                    ServerPlayNetworking.send(player, countersPacketPayload);
                    //#else
                    //$$ PacketByteBuf data = PacketByteBufs.create();
                    //$$ data.writeMap(
                    //$$         countersPacketPayload.currentRecords(),
                    //$$         PacketByteBuf::writeString,
                    //$$         (countersBuffer, counters) -> countersBuffer.writeMap(
                    //$$                 counters,
                    //$$                 PacketByteBuf::writeString,
                    //$$                 PacketByteBuf::writeString
                    //$$         )
                    //$$ );
                    //$$ ServerPlayNetworking.send(player, CountersPacketPayload.ID, data);
                    //#endif
                }));
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
        if (Objects.equals(CarpetSettings.commandPlayer, "false")) {
            FakePlayerControlManager.binds.forEach((player, data) -> FakePlayerControlManager.unbindPlayer(player, data.getValue()));
        }
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        FakePlayerControlManager.binds.forEach((player, data) -> FakePlayerControlManager.unbindPlayer(player, data.getValue()));
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