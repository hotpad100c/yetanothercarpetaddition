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

package mypals.ml.mixin.features.visualizers;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(TreeFeature.class)
public abstract class TreeGrowthObstacleUpdaterMixin {

    @Shadow
    private static boolean isVine(LevelSimulatedReader world, BlockPos pos) {return false;}

    @Unique
    private BlockPos pos;

    @Inject(method = "doPlace(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)Z", at = @At(value = "INVOKE", target =
    //#if MC >= 260100
            //$$ "Lnet/minecraft/world/level/levelgen/feature/TreeFeature;getMaxFreeTreeHeight(Lnet/minecraft/world/level/WorldGenLevel;ILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)I"
            //#else
            "Lnet/minecraft/world/level/levelgen/feature/TreeFeature;getMaxFreeTreeHeight(Lnet/minecraft/world/level/LevelSimulatedReader;ILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)I"
            //#endif
            , shift = At.Shift.AFTER))
    private void getObstacle(WorldGenLevel world, RandomSource random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootPlacerReplacer, BiConsumer<BlockPos, BlockState> trunkPlacerReplacer, FoliagePlacer.FoliageSetter blockPlacer, TreeConfiguration config, CallbackInfoReturnable<Boolean> cir) {
        if(YetAnotherCarpetAdditionRules.treeGrowthObstacleVisualize) {
            int height = config.trunkPlacer.baseHeight + config.trunkPlacer.heightRandA + config.trunkPlacer.heightRandB;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int i = 0; i <= height + 1; i++) {
                int j = config.minimumSize.getSizeAtHeight(height, i);

                for (int k = -j; k <= j; k++) {
                    for (int l = -j; l <= j; l++) {
                        mutable.setWithOffset(this.pos, k, i, l);
                        if (!config.trunkPlacer.isFree(world, mutable) || !config.ignoreVines && this.isVine(world, mutable)) {
                            YetAnotherCarpetAdditionServer.treeGrowthObstacleVisualzing.setVisualizer(world.getLevel(), mutable.immutable());
                        }
                    }
                }
            }
        }
    }

    @ModifyArg(method = "doPlace(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)Z", at= @At(value = "INVOKE", target =
    //#if MC >= 260100
            //$$ "Lnet/minecraft/world/level/levelgen/feature/TreeFeature;getMaxFreeTreeHeight(Lnet/minecraft/world/level/WorldGenLevel;ILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)I"
            //#else
            "Lnet/minecraft/world/level/levelgen/feature/TreeFeature;getMaxFreeTreeHeight(Lnet/minecraft/world/level/LevelSimulatedReader;ILnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)I"
            //#endif
            ),index = 2)
    private BlockPos getpos(BlockPos pos) {
        this.pos = pos;
        return pos;
    } 
}
