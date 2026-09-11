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

package mypals.ml.mixin.features.treeGrowthStats;

import mypals.ml.features.treeGrowthStats.TreeGrowthStatistics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 260300
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
//#endif

@Mixin(TreeFeature.class)
public class TreeFeatureStatMixin {

    // 26.3 folded TreeConfiguration into TreeFeature and made place() take the placement
    // arguments directly instead of a FeaturePlaceContext.
    //#if MC >= 260300
    //$$ private static final String PLACE = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z";
    //#else
    private static final String PLACE = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z";
    //#endif

    @Inject(method = PLACE, at = @At("HEAD"))
    private void yaca$beginTreeStats(
            //#if MC >= 260300
            //$$ WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos,
            //#else
            FeaturePlaceContext<TreeConfiguration> context,
            //#endif
            CallbackInfoReturnable<Boolean> cir) {
        //#if MC >= 260300
        //$$ TreeGrowthStatistics.begin((TreeFeature) (Object) this, level, random, pos);
        //#else
        TreeGrowthStatistics.begin(context);
        //#endif
    }

    @Inject(method = PLACE, at = @At("RETURN"), cancellable = true)
    private void yaca$endTreeStats(
            //#if MC >= 260300
            //$$ WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos,
            //#else
            FeaturePlaceContext<TreeConfiguration> context,
            //#endif
            CallbackInfoReturnable<Boolean> cir) {
        if (TreeGrowthStatistics.end()) {
            cir.setReturnValue(false);
        }
    }

    // The wrapped level must be the one place() itself works with: an argument on 26.3,
    // a local variable (index 2, right after `this` and the context) before that.
    //#if MC >= 260300
    //$$ @ModifyVariable(method = PLACE, at = @At("HEAD"), argsOnly = true, index = 1)
    //#else
    @ModifyVariable(method = PLACE, at = @At("STORE"), index = 2)
    //#endif
    private WorldGenLevel yaca$wrapLevel(WorldGenLevel level) {
        return TreeGrowthStatistics.wrapLevel(level);
    }
}
