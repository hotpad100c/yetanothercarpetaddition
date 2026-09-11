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

package mypals.ml.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mypals.ml.features.treeGrowthStats.TreeGrowthStatistics;
import mypals.ml.features.treeGrowthStats.TreeGrowthTask;
import mypals.ml.features.treeGrowthStats.TreeStatsExporter;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

//#if MC >= 1.21.11
import net.minecraft.server.permissions.Permissions;
//#endif

import java.nio.file.Path;
import java.util.Map;

public class TreeStatsCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("treeStats")
                .requires(source ->
                        //#if MC >= 1.21.11
                        source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        //#else
                        //$$ source.hasPermission(2))
                        //#endif
                .executes(context -> summary(context.getSource()))
                .then(Commands.literal("export")
                        .executes(context -> export(context.getSource())))
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource())))
                .then(Commands.literal("stop")
                        .executes(context -> stop(context.getSource())))
                .then(Commands.literal("grow")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("times", IntegerArgumentType.integer(1))
                                        .executes(context -> grow(context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                                IntegerArgumentType.getInteger(context, "times")))))));
    }

    private static int summary(CommandSourceStack source) {
        if (TreeGrowthStatistics.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "[YACA] 还没有统计数据。开启 saplingGrowthStatistics 后让树苗长成树即可。"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
                "[YACA] 树苗生长统计：共 " + TreeGrowthStatistics.totalTreeCount() + " 棵树，"
                        + TreeGrowthStatistics.snapshot().size() + " 个树种"), false);

        for (String species : TreeGrowthStatistics.speciesKeys()) {
            TreeGrowthStatistics.SpeciesData data = TreeGrowthStatistics.snapshot().get(species);
            if (data == null) {
                continue;
            }
            long logs = data.totalOf(TreeGrowthStatistics::isLog);
            long leaves = data.totalOf(TreeGrowthStatistics::isLeaves);
            long beehives = data.totalOf(b -> "beehive".equals(TreeGrowthStatistics.categoryOf(b)));
            String line = String.format("  %s: %d 棵, 原木 %d (%.2f/棵), 树叶 %d, 蜂巢 %d",
                    species, data.treeCount, logs,
                    data.treeCount == 0 ? 0.0 : (double) logs / data.treeCount, leaves, beehives);
            source.sendSuccess(() -> Component.literal(line), false);
        }
        source.sendSuccess(() -> Component.literal("  用 /treeStats export 导出网页"), false);
        return 1;
    }

    private static int export(CommandSourceStack source) {
        if (TreeGrowthStatistics.isEmpty()) {
            source.sendFailure(Component.literal("[YACA] 还没有统计数据，无法导出。"));
            return 0;
        }
        try {
            Path out = TreeStatsExporter.export();
            source.sendSuccess(() -> Component.literal("[YACA] 统计网页已生成：" + out), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("[YACA] 导出失败：" + e));
            return 0;
        }
    }

    private static int stop(CommandSourceStack source) {
        return TreeGrowthTask.stop(source) ? 1 : 0;
    }

    private static int grow(CommandSourceStack source, BlockPos pos, int times) {
        if (TreeGrowthTask.isRunning()) {
            source.sendFailure(Component.literal("[YACA] 已有催熟任务在运行，先等它结束。"));
            return 0;
        }
        if (!YetAnotherCarpetAdditionRules.saplingGrowthStatistics) {
            source.sendFailure(Component.literal(
                    "[YACA] 统计规则没开。先执行 /carpet saplingGrowthStatistics true"));
            return 0;
        }
        ServerLevel level = source.getLevel();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SaplingBlock)) {
            source.sendFailure(Component.literal("[YACA] 该坐标不是树苗：" + pos));
            return 0;
        }
        TreeGrowthTask.start(level, pos, state, times, source);
        source.sendSuccess(() -> Component.literal("[YACA] 开始快速催熟 " + times + " 次：" + pos), false);
        return 1;
    }

    private static int reset(CommandSourceStack source) {
        int before = TreeGrowthStatistics.totalTreeCount();
        int species = TreeGrowthStatistics.snapshot().size();
        TreeGrowthStatistics.reset();
        source.sendSuccess(() -> Component.literal(
                "[YACA] 已清空树苗生长统计（原有 " + before + " 棵树 / " + species + " 个树种）。"), false);
        return 1;
    }
}
