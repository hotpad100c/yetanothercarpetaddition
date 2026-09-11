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

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoliagePlacer.class)
public class FoliagePlacerMixin {

    @Inject(
            //#if MC >= 260100
            //$$ method = "createFoliage(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;ILnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageAttachment;II)V",
            //#else
            method = "createFoliage(Lnet/minecraft/world/level/LevelSimulatedReader;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;ILnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageAttachment;II)V",
            //#endif
            at = @At("HEAD"))
    private void generate(
            //#if MC >= 260100
            //$$ net.minecraft.world.level.WorldGenLevel world,
            //#else
            LevelSimulatedReader world,
            //#endif
            FoliagePlacer.FoliageSetter placer, RandomSource random, TreeConfiguration config, int trunkHeight, FoliagePlacer.FoliageAttachment treeNode, int foliageHeight, int radius, CallbackInfo ci) {
        if(mypals.ml.settings.YetAnotherCarpetAdditionRules.foliagePlacerVisualize) {
            mypals.ml.YetAnotherCarpetAdditionServer.foliageAttachment.setVisualizer((Level)world, treeNode.pos());
        }
    }


}
