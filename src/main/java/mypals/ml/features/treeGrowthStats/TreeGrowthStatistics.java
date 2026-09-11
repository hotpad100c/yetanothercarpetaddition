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

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    private static final ThreadLocal<Sample> CURRENT = new ThreadLocal<>();

    private static volatile boolean committed;

    private TreeGrowthStatistics() {
    }

    public static final class Sample {
        public final String species;
        public final BlockPos origin;
        public final Map<Integer, Block> blocks = new HashMap<>();

        Sample(String species, BlockPos origin) {
            this.species = species;
            this.origin = origin.immutable();
        }

        void record(BlockPos pos, BlockState state) {
            if (state.isAir()) {
                return;
            }
            if ("dirt".equals(categoryOf(state.getBlock()))) {
                return;
            }
            int dx = pos.getX() - origin.getX();
            int dy = pos.getY() - origin.getY();
            int dz = pos.getZ() - origin.getZ();
            if (Math.abs(dx) > 127 || Math.abs(dy) > 127 || Math.abs(dz) > 127) {
                return;
            }
            blocks.putIfAbsent(pack(dx, dy, dz), state.getBlock());
        }
    }

    public static boolean lastCommitted() {
        return committed;
    }

    public static void resetCommitted() {
        committed = false;
    }

    public static void begin(FeaturePlaceContext<TreeConfiguration> context) {
        committed = false;
        if (!YetAnotherCarpetAdditionRules.saplingGrowthStatistics) {
            return;
        }
        WorldGenLevel level = context.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        TreeConfiguration config = context.config();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Block log;
        //#if MC >= 260100
        //$$ log = config.trunkProvider.getState(level, random, origin).getBlock();
        //#else
        log = config.trunkProvider.getState(random, origin).getBlock();
        //#endif
        CURRENT.set(new Sample(speciesOf(log), origin));
    }

    public static boolean end() {
        Sample sample = CURRENT.get();
        CURRENT.remove();
        if (sample == null || sample.blocks.isEmpty()) {
            return false;
        }
        SpeciesData data = DATA.computeIfAbsent(sample.species, k -> new SpeciesData());
        synchronized (data) {
            data.treeCount++;
            sample.blocks.forEach((key, block) -> {
                data.posBlocks.computeIfAbsent(key, k -> new HashMap<>()).merge(block, 1, Integer::sum);
                data.totals.merge(block, 1L, Long::sum);
            });
        }
        committed = true;
        return true;
    }

    public static WorldGenLevel wrapLevel(WorldGenLevel real) {
        if (real == null || CURRENT.get() == null) {
            return real;
        }
        return (WorldGenLevel) Proxy.newProxyInstance(
                TreeGrowthStatistics.class.getClassLoader(),
                new Class<?>[]{WorldGenLevel.class},
                new RecordingLevel(real));
    }

    private static final class RecordingLevel implements InvocationHandler {
        private final WorldGenLevel real;

        RecordingLevel(WorldGenLevel real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("setBlock".equals(name) && args != null && args.length >= 2
                    && args[0] instanceof BlockPos && args[1] instanceof BlockState) {
                Sample sample = CURRENT.get();
                if (sample != null) {
                    sample.record((BlockPos) args[0], (BlockState) args[1]);
                    return Boolean.TRUE;
                }
            }
            if ("equals".equals(name) && args != null && args.length == 1) {
                return proxy == args[0];
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(name)) {
                return "YACA-TreeGrowthStatistics-Level";
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
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
        try {
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        } catch (Exception e) {
            return "unknown";
        }
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
