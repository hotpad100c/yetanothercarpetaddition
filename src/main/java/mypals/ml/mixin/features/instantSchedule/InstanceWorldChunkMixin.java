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
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class InstanceWorldChunkMixin {


    @Shadow
    @Final
    private ChunkTickScheduler<Block> blockTickScheduler;

    @Shadow
    @Final
    private ChunkTickScheduler<Fluid> fluidTickScheduler;

    @Inject(method = "addChunkTickSchedulers", at = @At("HEAD"))
    public void setWorld(ServerWorld world, CallbackInfo ci) {
        ((InstanceChunkTickSchedule) this.blockTickScheduler)
                .setServerWorld(world);
        ((InstanceChunkTickSchedule) this.fluidTickScheduler)
                .setServerWorld(world);
    }
}
