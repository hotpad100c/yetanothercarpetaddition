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

package mypals.ml.mixin.features.mobAIVisualize;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.MobAIVisualizer;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.mobAIVisualize;

@Mixin(MobEntity.class)
public class MobAIMixin {
    @Shadow
    @Final
    protected GoalSelector goalSelector;

    @Shadow
    @Final
    protected GoalSelector targetSelector;

    @Inject(method = "tickNewAi", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/EntityNavigation;tick()V"))
    private void displayTargetAboveHead(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.getWorld().isClient() || !mobAIVisualize) return;
        YetAnotherCarpetAdditionServer.mobAIVisualizer.setVisualizer(
                (ServerWorld) mob.getWorld(),
                mob,
                mob.getPos().add(0, mob.getHeight() + 0.5, 0),
                new MobAIVisualizer.MobAIData(
                        mob,
                        this.goalSelector,
                        this.targetSelector
                )
        );
    }
}
