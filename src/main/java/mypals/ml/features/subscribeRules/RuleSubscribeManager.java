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

package mypals.ml.features.subscribeRules;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mypals.ml.features.waypoint.WaypointManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RuleSubscribeManager {
    public static File configFile;
    private static int MAX_SUBSCRIBES = 10;
    public static Map<String, CarpetRule<?>> subscribed = new HashMap<>();

    public static void init(MinecraftServer server) {
        File worldDir = server.getSavePath(WorldSavePath.ROOT).toFile();
        File configDir = new File(worldDir, "YACA");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "subs.json");
        loadSub();
        if (subscribed == null) {
            subscribed = new HashMap<>();
        }
    }

    public static void loadSub() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<Set<String>>() {
                }.getType();
                Set<String> names = new Gson().fromJson(reader, type);

                Map<String, CarpetRule<?>> ruleMap = CarpetServer.settingsManager.getCarpetRules().stream()
                        .filter(rule -> rule != null && rule.name() != null)
                        .collect(Collectors.toMap(CarpetRule::name, rule -> rule, (r1, r2) -> r1));

                for (String name : names) {
                    CarpetRule<?> rule = ruleMap.get(name);
                    if (rule != null) {
                        subscribed.put(name, rule);
                    }
                }

                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveSub();
    }

    public static void saveSub() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(subscribed.keySet(), writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void subscribeRule(String name, ServerCommandSource source) {
        if (subscribed.containsKey(name)) {
            source.sendFeedback(() -> Text.of("Unsubscribed to rule: " + name), true);
            subscribed.remove(name);
        } else {
            CarpetServer.settingsManager.getCarpetRules().stream()
                    .forEach(carpetRule -> {
                        if (carpetRule.name().equals(name)) {
                            if (subscribed.size() >= MAX_SUBSCRIBES) {
                                source.sendError(Text.of("You have reached the maximum number of subscribed rules."));
                                return;
                            }
                            subscribed.put(name, carpetRule);
                            source.sendFeedback(() -> Text.of("Subscribed to rule: " + name), true);
                        }
                    });
        }
        saveSub();
    }
}
