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

package mypals.ml.features.treeGrowthStats;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class TreeGrowthStatistics {

    private static final Map<String, SpeciesData> DATA = new ConcurrentHashMap<>();

    private static final Map<Block, String> CATEGORY_CACHE = new ConcurrentHashMap<>();

    private static final Map<Block, String> ID_CACHE = new ConcurrentHashMap<>();

    private TreeGrowthStatistics() {
    }

    public static final class SpeciesData {
        public int treeCount;
        public final Map<Integer, Map<Block, Integer>> posBlocks = new HashMap<>();
        public final Map<Block, Long> totals = new LinkedHashMap<>();

        public long totalOf(Predicate<Block> filter) {
            long sum = 0;
            for (Map.Entry<Block, Long> e : totals.entrySet()) {
                if (filter.test(e.getKey())) {
                    sum += e.getValue();
                }
            }
            return sum;
        }
    }

    public static void merge(String species, int trees, Map<Integer, Map<Block, Integer>> incoming) {
        if (trees <= 0 || incoming.isEmpty()) {
            return;
        }
        SpeciesData data = DATA.computeIfAbsent(species, k -> new SpeciesData());
        data.treeCount += trees;
        for (Map.Entry<Integer, Map<Block, Integer>> e : incoming.entrySet()) {
            Map<Block, Integer> target = data.posBlocks.computeIfAbsent(e.getKey(), k -> new HashMap<>());
            for (Map.Entry<Block, Integer> b : e.getValue().entrySet()) {
                target.merge(b.getKey(), b.getValue(), Integer::sum);
                data.totals.merge(b.getKey(), (long) b.getValue(), Long::sum);
            }
        }
    }

    public static Map<String, SpeciesData> snapshot() {
        return DATA;
    }

    public static boolean isEmpty() {
        return DATA.isEmpty();
    }

    public static void reset() {
        DATA.clear();
    }

    public static int totalTreeCount() {
        int sum = 0;
        for (SpeciesData d : DATA.values()) {
            sum += d.treeCount;
        }
        return sum;
    }

    public static List<String> speciesKeys() {
        List<String> keys = new ArrayList<>(DATA.keySet());
        keys.sort(Comparator.naturalOrder());
        return keys;
    }

    public static String speciesOf(Block log) {
        String id = blockId(log);
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        for (String suffix : new String[]{"_log", "_wood", "_stem", "_hyphae"}) {
            if (path.endsWith(suffix)) {
                path = path.substring(0, path.length() - suffix.length());
                break;
            }
        }
        return path.isEmpty() || path.equals("unknown") ? "unknown" : path;
    }

    public static String categoryOf(Block block) {
        String cached = CATEGORY_CACHE.get(block);
        if (cached != null) {
            return cached;
        }
        String category = computeCategory(block);
        CATEGORY_CACHE.put(block, category);
        return category;
    }

    private static String computeCategory(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.is(BlockTags.LOGS)) {
            return "log";
        }
        if (state.is(BlockTags.LEAVES)) {
            return "leaves";
        }
        if (block == Blocks.BEEHIVE || block == Blocks.BEE_NEST) {
            return "beehive";
        }
        if (blockId(block).endsWith("_sapling") || blockId(block).endsWith("_propagule")) {
            return "sapling";
        }
        if (state.is(BlockTags.DIRT) || block == Blocks.PODZOL || block == Blocks.MOSS_BLOCK) {
            return "dirt";
        }
        return "other";
    }

    public static boolean isLog(Block block) {
        return "log".equals(categoryOf(block));
    }

    public static boolean isLeaves(Block block) {
        return "leaves".equals(categoryOf(block));
    }

    public static String blockId(Block block) {
        String cached = ID_CACHE.get(block);
        if (cached != null) {
            return cached;
        }
        String id;
        try {
            id = BuiltInRegistries.BLOCK.getKey(block).toString();
        } catch (Exception e) {
            id = "unknown";
        }
        ID_CACHE.put(block, id);
        return id;
    }

    public static List<Map.Entry<Block, Long>> sortedTotals(SpeciesData data) {
        List<Map.Entry<Block, Long>> list = new ArrayList<>(data.totals.entrySet());
        list.sort(Comparator.<Map.Entry<Block, Long>>comparingLong(Map.Entry::getValue).reversed());
        return list;
    }

    public static int pack(int dx, int dy, int dz) {
        return ((dx + 128) & 0xFF) << 16 | ((dy + 128) & 0xFF) << 8 | ((dz + 128) & 0xFF);
    }

    public static int unpackX(int key) {
        return ((key >> 16) & 0xFF) - 128;
    }

    public static int unpackY(int key) {
        return ((key >> 8) & 0xFF) - 128;
    }

    public static int unpackZ(int key) {
        return (key & 0xFF) - 128;
    }
}
