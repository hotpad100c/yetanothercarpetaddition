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

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class TreeGrowthTask {

    private static final int PER_TICK = 500;

    private static Task active;
    private static boolean registered;

    private TreeGrowthTask() {
    }

    private static final class Task {
        ServerLevel level;
        BlockPos pos;
        BlockState sapling;
        CommandSourceStack source;
        int total;
        int done;
        ServerBossEvent bar;
    }

    public static boolean isRunning() {
        return active != null;
    }

    public static void start(ServerLevel level, BlockPos pos, BlockState sapling, int times, CommandSourceStack source) {
        ensureRegistered();
        Task t = new Task();
        t.level = level;
        t.pos = pos.immutable();
        t.sapling = sapling.setValue(SaplingBlock.STAGE, 1);
        t.total = times;
        t.source = source;
        t.bar = makeBar(Component.literal("树苗统计 0/" + times));
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            t.bar.addPlayer(player);
        }
        active = t;
    }

    private static void ensureRegistered() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(TreeGrowthTask::tick);
    }

    private static ServerBossEvent makeBar(Component title) {
        //#if MC >= 260100
        //$$ return new ServerBossEvent(java.util.UUID.randomUUID(), title, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        //#else
        return new ServerBossEvent(title, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        //#endif
    }

    private static void tick(MinecraftServer server) {
        Task t = active;
        if (t == null) {
            return;
        }
        for (int i = 0; i < PER_TICK && t.done < t.total; i++) {
            t.level.setBlock(t.pos, t.sapling, 3);
            TreeGrowthStatistics.resetCommitted();
            ((SaplingBlock) t.sapling.getBlock()).advanceTree(t.level, t.pos, t.sapling, t.level.getRandom());
            if (!TreeGrowthStatistics.lastCommitted()) {
                finish(t, "第 " + (t.done + 1) + "/" + t.total
                        + " 次没有长成树，已中止（检查上方是否有空间、下方是否为泥土）");
                return;
            }
            t.done++;
        }
        t.bar.setProgress(t.total == 0 ? 0F : (float) t.done / t.total);
        t.bar.setName(Component.literal("树苗统计 " + t.done + "/" + t.total));
        if (t.done >= t.total) {
            finish(t, null);
        }
    }

    public static boolean stop(CommandSourceStack requester) {
        Task t = active;
        if (t == null) {
            requester.sendFailure(Component.literal("[YACA] 当前没有正在运行的催熟任务。"));
            return false;
        }
        finish(t, "被手动停止");
        requester.sendSuccess(() -> Component.literal("[YACA] 已停止催熟任务。"), false);
        return true;
    }

    private static void finish(Task t, String reason) {
        active = null;
        t.bar.removeAllPlayers();
        String head = reason == null
                ? "[YACA] 完成 " + t.total + " 次催熟"
                : "[YACA] 催熟任务中止：" + reason + "（完成 " + t.done + "/" + t.total + "）";
        String tail;
        try {
            tail = "；累计样本 " + TreeGrowthStatistics.totalTreeCount() + " 棵；网页：" + TreeStatsExporter.export();
        } catch (Exception e) {
            tail = "；导出网页失败：" + e;
        }
        final String msg = head + tail;
        t.source.sendSuccess(() -> Component.literal(msg), false);
    }
}
