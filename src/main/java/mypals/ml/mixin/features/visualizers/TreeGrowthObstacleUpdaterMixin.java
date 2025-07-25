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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
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
    private static boolean isVine(TestableWorld world, BlockPos pos) {return false;}

    @Unique
    private BlockPos pos;

    @Inject(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Lnet/minecraft/world/gen/foliage/FoliagePlacer$BlockPlacer;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/TreeFeature;getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I" , shift = At.Shift.AFTER))
    private void getObstacle(StructureWorldAccess world, Random random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootPlacerReplacer, BiConsumer<BlockPos, BlockState> trunkPlacerReplacer, FoliagePlacer.BlockPlacer blockPlacer, TreeFeatureConfig config, CallbackInfoReturnable<Boolean> cir) {
        if(YetAnotherCarpetAdditionRules.treeGrowthObstacleVisualize) {
            int height = config.trunkPlacer.baseHeight + config.trunkPlacer.firstRandomHeight + config.trunkPlacer.secondRandomHeight;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int i = 0; i <= height + 1; i++) {
                int j = config.minimumSize.getRadius(height, i);

                for (int k = -j; k <= j; k++) {
                    for (int l = -j; l <= j; l++) {
                        mutable.set(this.pos, k, i, l);
                        if (!config.trunkPlacer.canReplaceOrIsLog(world, mutable) || !config.ignoreVines && this.isVine(world, mutable)) {
                            YetAnotherCarpetAdditionServer.treeGrowthObstacleVisualzing.setVisualizer(world.toServerWorld(), mutable.toImmutable());
                        }
                    }
                }
            }
        }
    }

    @ModifyArg(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Lnet/minecraft/world/gen/foliage/FoliagePlacer$BlockPlacer;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)Z", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/TreeFeature;getTopPosition(Lnet/minecraft/world/TestableWorld;ILnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/TreeFeatureConfig;)I"),index = 2)
    private BlockPos getpos(BlockPos pos) {
        this.pos = pos;
        return pos;
    } 
}
