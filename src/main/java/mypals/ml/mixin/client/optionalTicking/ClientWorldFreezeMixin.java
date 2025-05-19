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

package mypals.ml.mixin.client.optionalTicking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldFreezeMixin extends World {
    protected ClientWorldFreezeMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockTickBlockEntities(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingTime) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockTickEntities(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity) && YetAnotherCarpetAdditionRules.stopTickingEntities) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "tickEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;tickBlockEntities()V"))
    private void blockTickClientChunkManager(ClientWorld instance, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlockEntities) {
            original.call(instance);
        }
    }

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;tick(Ljava/util/function/BooleanSupplier;Z)V"))
    private void blockTickClientChunkManager(ClientChunkManager instance, BooleanSupplier shouldKeepTicking, boolean tickChunks, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingChunkManager) {
            original.call(instance, shouldKeepTicking, false);
        }

    }

}
