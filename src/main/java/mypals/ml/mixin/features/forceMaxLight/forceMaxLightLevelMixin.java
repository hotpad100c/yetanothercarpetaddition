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

package mypals.ml.mixin.features.forceMaxLight;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLightProvider.class)
public abstract class forceMaxLightLevelMixin {
    @Shadow
    @Final
    private LongOpenHashSet blockPositionsToCheck;

    @Shadow
    @Final
    private LongArrayFIFOQueue field_44734;

    @Shadow
    @Final
    private LongArrayFIFOQueue field_44735;

    @Shadow
    protected abstract void clearChunkCache();

    @WrapMethod(method = "getLightLevel")
    private int getLightLevel(BlockPos pos, Operation<Integer> original) {
        return YetAnotherCarpetAdditionRules.forceMaxLightLevel ? 15 : original.call(pos);
    }

    @Inject(
            method = "checkBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldCheckBlock(BlockPos pos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

}
