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

package mypals.ml.mixin.features.betterCommmand;

import mypals.ml.features.betterCommands.GamerulesDefaultValueSorter;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 12102
//$$ import net.minecraft.resource.featuretoggle.FeatureSet;
//#endif

import java.util.Map;

@Mixin(GameRules.class)
public class GamerulesMixin {
    @Shadow
    @Final
    public Map<GameRules.Key<?>, GameRules.Rule<?>> rules;

    @Inject(
            //#if MC < 12102
            method = "<init>()V",
            //#else
            //$$ method = "<init>(Lnet/minecraft/resource/featuretoggle/FeatureSet;)V",
            //#endif
            at = @At(
                    "RETURN"
            )
    )
    public void createGameRules(CallbackInfo ci) {
        GamerulesDefaultValueSorter.gamerulesDefaultValues.clear();
        this.rules.forEach((key, rule) -> {
            GamerulesDefaultValueSorter
                    .gamerulesDefaultValues.put(key, rule.toString());

        });
    }

    @Inject(
            //#if MC < 12102
            method = "<init>(Ljava/util/Map;)V",
            //#else
            //$$ method = "<init>(Ljava/util/Map;Lnet/minecraft/resource/featuretoggle/FeatureSet;)V",
            //#endif
            at = @At(
                    "RETURN"
            )
    )
    public void createGameRules2(Map rules,
                                 //#if MC >= 12102
                                 //$$ FeatureSet enabledFeatures,
                                 //#endif
                                 CallbackInfo ci) {
        GamerulesDefaultValueSorter.gamerulesDefaultValues.clear();
        rules.forEach((key, rule) -> {
            GamerulesDefaultValueSorter
                    .gamerulesDefaultValues.put((GameRules.Key<?>) key, rule.toString());

        });
    }
    
}
