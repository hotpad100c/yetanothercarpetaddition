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
import mypals.ml.YetAnotherCarpetAdditionClient;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientWorldFreezeMixin extends Level {
    protected ClientWorldFreezeMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry,
                //#if MC < 12102
                //$$ profiler,
                //#endif
                isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockTickBlockEntities(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingTime || YetAnotherCarpetAdditionRules.stopTickingWeather || YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingWeather) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickNonPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockTickEntities(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Player) && (YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingEntities)) {
            ci.cancel();

        }
    }

    //#if MC < 12109
    @WrapOperation(method = "tickEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tickBlockEntities()V"))
    private void blockTickClientChunkManager(ClientLevel instance, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlockEntities || !YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingBlockEntities) {
            original.call(instance);
        }
    }
    //#endif

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache;tick(Ljava/util/function/BooleanSupplier;Z)V"))
    private void blockTickClientChunkManager(ClientChunkCache instance, BooleanSupplier shouldKeepTicking, boolean tickChunks, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingChunkManager || YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingChunkManager) {
            original.call(instance, shouldKeepTicking, false);
        }

    }

}
