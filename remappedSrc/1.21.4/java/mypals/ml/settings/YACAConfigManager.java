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

package mypals.ml.settings;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YACAConfigManager {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path YACA_DIR = CONFIG_DIR.resolve("YACA");
    private static final Path FAVORITE_RULES_FILE = YACA_DIR.resolve("favoriteRules.conf");

    public static void initializeConfig() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            if (!Files.exists(YACA_DIR)) {
                Files.createDirectories(YACA_DIR);
            }

            if (!Files.exists(FAVORITE_RULES_FILE)) {
                Files.createFile(FAVORITE_RULES_FILE);
                Files.writeString(FAVORITE_RULES_FILE, "# YACA Favorite Rules Configuration\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path getFavoriteRulesConfigPath() {
        return FAVORITE_RULES_FILE;
    }

    public static List<String> readFavoriteRules() {
        try {
            if (Files.exists(FAVORITE_RULES_FILE)) {
                return Files.readAllLines(FAVORITE_RULES_FILE)
                        .stream()
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void writeFavoriteRules(List<String> rules) {
        try {
            if (!Files.exists(YACA_DIR)) {
                Files.createDirectories(YACA_DIR);
            }

            List<String> content = new ArrayList<>();
            content.add("# YACA Favorite Rules Configuration\n");
            if (rules != null) {
                content.addAll(rules);
            }

            Files.write(FAVORITE_RULES_FILE, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addFavoriteRule(String rule) {
        if (rule == null || rule.trim().isEmpty()) {
            return;
        }

        List<String> rules = readFavoriteRules();
        if (!rules.contains(rule)) {
            rules.add(rule);
            writeFavoriteRules(rules);
        }
    }

    public static void removeFavoriteRule(String rule) {
        if (rule == null || rule.trim().isEmpty()) {
            return;
        }

        List<String> rules = readFavoriteRules();
        rules.removeIf(r -> r.equalsIgnoreCase(rule));
        writeFavoriteRules(rules);
    }
}
