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

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class SampleContext {

    private static final ThreadLocal<SampleContext> CURRENT = new ThreadLocal<>();

    private static volatile boolean active;

    private final WorldSnapshot snapshot;
    private final BlockState[] overlay;
    private final int[] stamp;
    private final Map<Long, BlockState> overflow = new HashMap<>();
    private final Map<Integer, Block> blocks = new HashMap<>();
    private final Map<String, Map<Integer, Map<Block, Integer>>> pending = new HashMap<>();
    private final Map<String, Integer> pendingTrees = new HashMap<>();
    private int version;
    private String species;
    private int originX;
    private int originY;
    private int originZ;
    private RandomSource random;
    private boolean dirty;

    private SampleContext(WorldSnapshot snapshot) {
        this.snapshot = snapshot;
        this.overlay = new BlockState[snapshot.cellCount()];
        this.stamp = new int[snapshot.cellCount()];
    }

    public static SampleContext get() {
        if (!active) {
            return null;
        }
        return CURRENT.get();
    }

    public static void deactivate() {
        active = false;
    }

    public static SampleContext acquire(WorldSnapshot snapshot) {
        SampleContext ctx = CURRENT.get();
        if (ctx == null || ctx.snapshot != snapshot) {
            ctx = new SampleContext(snapshot);
            CURRENT.set(ctx);
        }
        active = true;
        return ctx;
    }

    public static void release() {
        CURRENT.remove();
    }

    public RandomSource random() {
        return random;
    }

    public void beginSample(BlockPos origin, RandomSource source) {
        version++;
        if (version == Integer.MAX_VALUE) {
            java.util.Arrays.fill(stamp, 0);
            version = 1;
        }
        overflow.clear();
        blocks.clear();
        species = null;
        dirty = false;
        originX = origin.getX();
        originY = origin.getY();
        originZ = origin.getZ();
        random = source;
    }

    public boolean endSample() {
        random = null;
        if (species == null) {
            return false;
        }
        Map<Integer, Map<Block, Integer>> target =
                pending.computeIfAbsent(species, k -> new HashMap<>());
        blocks.forEach((key, block) ->
                target.computeIfAbsent(key, k -> new HashMap<>()).merge(block, 1, Integer::sum));
        pendingTrees.merge(species, 1, Integer::sum);
        dirty = true;
        return true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void flush() {
        if (pending.isEmpty()) {
            return;
        }
        pending.forEach((species, blocks) -> {
            Integer trees = pendingTrees.get(species);
            TreeGrowthStatistics.merge(species, trees == null ? 0 : trees, blocks);
        });
        pending.clear();
        pendingTrees.clear();
        dirty = false;
    }

    public WorldSnapshot snapshot() {
        return snapshot;
    }

    public BlockState getBlockState(BlockPos pos) {
        int idx = snapshot.index(pos.getX(), pos.getY(), pos.getZ());
        if (idx < 0) {
            BlockState out = overflow.get(pos.asLong());
            return out != null ? out : Blocks.AIR.defaultBlockState();
        }
        if (stamp[idx] == version) {
            return overlay[idx];
        }
        return snapshot.states[idx];
    }

    public void setBlock(BlockPos pos, BlockState state) {
        int idx = snapshot.index(pos.getX(), pos.getY(), pos.getZ());
        if (idx < 0) {
            overflow.put(pos.asLong(), state);
        } else {
            stamp[idx] = version;
            overlay[idx] = state;
        }
        record(pos, state);
    }

    public int surfaceY(int x, int z) {
        return snapshot.surfaceY(x, z);
    }

    private void record(BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return;
        }
        int dx = pos.getX() - originX;
        int dy = pos.getY() - originY;
        int dz = pos.getZ() - originZ;
        if (Math.abs(dx) > 127 || Math.abs(dy) > 127 || Math.abs(dz) > 127) {
            return;
        }
        int key = TreeGrowthStatistics.pack(dx, dy, dz);
        if (blocks.containsKey(key)) {
            return;
        }
        Block block = state.getBlock();
        String category = TreeGrowthStatistics.categoryOf(block);
        if ("dirt".equals(category)) {
            return;
        }
        blocks.put(key, block);
        if (species == null && "log".equals(category)) {
            species = TreeGrowthStatistics.speciesOf(block);
        }
    }
}
