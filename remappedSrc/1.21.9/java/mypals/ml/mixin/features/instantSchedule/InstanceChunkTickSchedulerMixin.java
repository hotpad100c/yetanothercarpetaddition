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

package mypals.ml.mixin.features.instantSchedule;

import mypals.ml.interfaces.InstanceChunkTickSchedule;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunkTicks.class)
public abstract class InstanceChunkTickSchedulerMixin<T> implements InstanceChunkTickSchedule {

    @Unique
    private ServerLevel serverWorld;

    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    public void shouldInstantTick(ScheduledTick<T> orderedTick, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.instantSchedule) {
            if (orderedTick.type() instanceof Block) {
                serverWorld.tickBlock(orderedTick.pos(), (Block) orderedTick.type());
                ci.cancel();
            } else {
                serverWorld.tickFluid(orderedTick.pos(), (Fluid) orderedTick.type());
                ci.cancel();
            }
        }
    }

    @Override
    public void setServerWorld(ServerLevel serverWorld) {
        this.serverWorld = serverWorld;
    }
}

