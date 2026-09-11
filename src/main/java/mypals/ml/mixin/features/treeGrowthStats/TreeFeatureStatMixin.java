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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public class TreeFeatureStatMixin {

    private static final String PLACE = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z";

    @Inject(method = PLACE, at = @At("HEAD"))
    private void yaca$beginTreeStats(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        TreeGrowthStatistics.begin(context);
    }

    @Inject(method = PLACE, at = @At("RETURN"), cancellable = true)
    private void yaca$endTreeStats(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (TreeGrowthStatistics.end()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = PLACE, at = @At("STORE"), index = 2)
    private WorldGenLevel yaca$wrapLevel(WorldGenLevel level) {
        return TreeGrowthStatistics.wrapLevel(level);
    }
}
