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

import carpet.utils.Translations;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class TreeGrowthTask {

    private static final int MAX_CONSECUTIVE_FAILS = 5000;

    private static final int REPORT_INTERVAL = 4096;

    private static final int UI_EVERY_TICKS = 10;

    private static volatile Task active;

    private TreeGrowthTask() {
    }

    private static final class Task {
        ServerLevel level;
        BlockPos pos;
        BlockState planted;
        SaplingBlock sapling;
        CommandSourceStack source;
        WorldSnapshot snapshot;
        int total;
        int threads;
        final AtomicInteger done = new AtomicInteger();
        final AtomicInteger fails = new AtomicInteger();
        final AtomicInteger remaining = new AtomicInteger();
        final List<SampleContext> contexts = new CopyOnWriteArrayList<>();
        ServerBossEvent bar;
        ExecutorService pool;
        long snapshotNanos;
        long startedAt;
        volatile String error;
        volatile boolean cancelled;
        int uiCooldown;
    }

    public static boolean isRunning() {
        return active != null;
    }

    private static ServerBossEvent makeBar(Component title) {
        //#if MC >= 260100
        //$$ return new ServerBossEvent(java.util.UUID.randomUUID(), title, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        //#else
        return new ServerBossEvent(title, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        //#endif
    }

    public static boolean run(ServerLevel level, BlockPos pos, BlockState sapling, int times, CommandSourceStack source) {
        if (active != null) {
            return false;
        }
        Task t = new Task();
        t.level = level;
        t.pos = pos.immutable();
        t.planted = sapling.setValue(SaplingBlock.STAGE, 1);
        t.sapling = (SaplingBlock) t.planted.getBlock();
        t.source = source;
        t.total = times;
        long snapshotStart = System.nanoTime();
        t.snapshot = WorldSnapshot.capture(level, t.pos);
        t.snapshotNanos = System.nanoTime() - snapshotStart;
        t.startedAt = System.nanoTime();
        t.bar = makeBar(Component.literal(String.format(Translations.tr("command.treeStats.bar"), 0, times)));
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            t.bar.addPlayer(player);
        }
        t.bar.setProgress(0F);
        t.threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        t.remaining.set(t.threads);
        active = t;
        setFrozen(level.getServer(), true);
        t.pool = Executors.newFixedThreadPool(t.threads, r -> {
            Thread thread = new Thread(r, "YACA-TreeStats");
            thread.setDaemon(true);
            return thread;
        });
        int per = times / t.threads;
        int extra = times % t.threads;
        for (int i = 0; i < t.threads; i++) {
            int quota = per + (i < extra ? 1 : 0);
            long seed = t.snapshot.seed + i * 7919L + 13L;
            t.pool.execute(() -> worker(t, quota, seed));
        }
        t.pool.shutdown();
        return true;
    }

    private static void setFrozen(MinecraftServer server, boolean frozen) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                frozen ? "tick freeze" : "tick unfreeze");
    }

    private static void worker(Task t, int quota, long seed) {
        SampleContext ctx = null;
        try {
            ctx = SampleContext.acquire(t.snapshot);
            t.contexts.add(ctx);
            RandomSource random = RandomSource.create(seed);
            int success = 0;
            int consecutive = 0;
            int sinceReport = 0;
            while (success < quota && !t.cancelled) {
                ctx.beginSample(t.pos, random);
                t.sapling.advanceTree(t.level, t.pos, t.planted, random);
                if (ctx.endSample()) {
                    success++;
                    consecutive = 0;
                    if (++sinceReport >= REPORT_INTERVAL) {
                        t.done.addAndGet(sinceReport);
                        sinceReport = 0;
                    }
                } else {
                    consecutive++;
                    t.fails.incrementAndGet();
                    if (consecutive >= MAX_CONSECUTIVE_FAILS) {
                        t.error = String.format(Translations.tr("command.treeStats.growFailed"), consecutive);
                        t.cancelled = true;
                        break;
                    }
                }
            }
            t.done.addAndGet(sinceReport);
        } catch (Throwable e) {
            t.error = String.valueOf(e);
            t.cancelled = true;
        } finally {
            SampleContext.release();
            if (t.remaining.decrementAndGet() == 0) {
                finish(t);
            }
        }
    }

    public static void tick(MinecraftServer server) {
        Task t = active;
        if (t == null) {
            return;
        }
        if (--t.uiCooldown > 0) {
            return;
        }
        t.uiCooldown = UI_EVERY_TICKS;
        updateBar(t);
    }

    private static void updateBar(Task t) {
        int done = t.done.get();
        t.bar.setProgress(t.total == 0 ? 1F : Math.min(1F, (float) done / t.total));
        int fails = t.fails.get();
        t.bar.setName(Component.literal(fails > 0
                ? String.format(Translations.tr("command.treeStats.barRetry"), done, t.total, fails)
                : String.format(Translations.tr("command.treeStats.bar"), done, t.total)));
    }

    private static void finish(Task t) {
        t.level.getServer().execute(() -> {
            active = null;
            setFrozen(t.level.getServer(), false);
            t.bar.setProgress(1F);
            t.bar.removeAllPlayers();
            for (SampleContext ctx : t.contexts) {
                ctx.flush();
            }
            SampleContext.deactivate();
            String path;
            try {
                path = String.valueOf(TreeStatsExporter.export());
            } catch (Exception e) {
                path = String.valueOf(e);
            }
            final String message;
            if (t.error != null) {
                message = "[YACA] " + String.format(Translations.tr("command.treeStats.growAborted"),
                        t.error, t.done.get(), t.total);
            } else {
                double snapshotSeconds = t.snapshotNanos / 1.0e9;
                double simSeconds = (System.nanoTime() - t.startedAt) / 1.0e9;
                double rate = simSeconds > 0 ? t.total / simSeconds : 0;
                message = "[YACA] " + String.format(Translations.tr("command.treeStats.growDone"),
                        t.total, TreeGrowthStatistics.totalTreeCount(), path)
                        + " " + String.format(Translations.tr("command.treeStats.growSpeed"),
                        snapshotSeconds, simSeconds, rate, t.fails.get());
            }
            t.source.sendSuccess(() -> Component.literal(message), false);
        });
    }

    public static boolean stop(CommandSourceStack requester) {
        Task t = active;
        if (t == null) {
            requester.sendFailure(Component.literal("[YACA] " + Translations.tr("command.treeStats.notRunning")));
            return false;
        }
        t.cancelled = true;
        t.error = Translations.tr("command.treeStats.stopped");
        return true;
    }
}
