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
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTickScheduler.class)
public abstract class InstanceChunkTickSchedulerMixin<T> implements InstanceChunkTickSchedule {

    @Unique
    private ServerWorld serverWorld;

    @Inject(method = "scheduleTick", at = @At("HEAD"), cancellable = true)
    public void shouldInstantTick(OrderedTick<T> orderedTick, CallbackInfo ci) {
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
    public void setServerWorld(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }
}

