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

package mypals.ml.mixin.features.treefarm;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
//#if MC >= 260300
//$$ import net.minecraft.world.level.block.BonemealSource;
//#endif
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {
    @Inject(method = "isBonemealSuccess", at = @At("HEAD"), cancellable = true)
    public void canGrow(Level world, RandomSource random, BlockPos pos, BlockState state,
                        // 26.3 added the BonemealSource argument (INTERACTION / MOB) to isBonemealSuccess
                        //#if MC >= 260300
                        //$$ BonemealSource source,
                        //#endif
                        CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue ((double)world.getRandom().nextFloat() < YetAnotherCarpetAdditionRules.bonemealSuccessProbability);
    }
}
