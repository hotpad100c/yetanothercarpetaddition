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

import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoliagePlacer.class)
public class FoliagePlacerMixin {

    @Inject(method = "Lnet/minecraft/world/gen/foliage/FoliagePlacer;generate(Lnet/minecraft/world/TestableWorld;Lnet/minecraft/world/gen/foliage/FoliagePlacer$BlockPlacer;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;ILnet/minecraft/world/gen/foliage/FoliagePlacer$TreeNode;II)V",at = @At("HEAD"))
    private void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, CallbackInfo ci) {
        if(mypals.ml.settings.YetAnotherCarpetAdditionRules.foliagePlacerVisualize) {
            mypals.ml.YetAnotherCarpetAdditionServer.foliageAttachment.setVisualizer((World)world, treeNode.getCenter());
        }
    }


}
