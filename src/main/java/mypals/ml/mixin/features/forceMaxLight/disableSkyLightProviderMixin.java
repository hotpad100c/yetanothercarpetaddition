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

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyLightEngine.class)
public class disableSkyLightProviderMixin {
    @Inject(
            method = "checkNode",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates1(long blockPos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "propagateDecrease",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates2(long blockPos, long l, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "propagateIncrease",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates3(long blockPos, long l, int lightLevel, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "propagateLightSources",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldPropagateLight(ChunkPos chunkPos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    /*@Inject(
            method = "setColumnEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldSetColumnEnabled(ChunkPos pos, boolean retainData, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }*/
}
