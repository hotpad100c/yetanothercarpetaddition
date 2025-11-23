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

package mypals.ml.mixin.features;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.selectiveFreeze.SelectiveFreezeManager;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level {
    @Shadow
    @Final
    private EntityTickList entityTickList;

    protected ServerWorldMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry,
                //#if MC <12102
                //$$ profiler,
                //#endif
                isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract TickRateManager tickRateManager();

    @Shadow
    @Final
    private ServerChunkCache chunkSource;

    @Shadow
    protected abstract void tickPassenger(Entity vehicle, Entity passenger);

    @Shadow
    @Final
    private LevelTicks<Fluid> fluidTicks;

    @Shadow
    @Final
    private LevelTicks<Block> blockTicks;

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Inject(
            method = "advanceWeatherCycle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickWeather(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingWeather || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickBlocks(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickFluid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickFluid(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingFluids || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickNonPassenger",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickEntity(Entity entity, CallbackInfo ci) {
        if ((YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities) && !(entity instanceof Player)) {
            ci.cancel();
        }
        if (entity.getTags().contains("DoNotTick")) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "tickNonPassenger",
            at = @At(target = "Lnet/minecraft/world/entity/Entity;tick()V", value = "INVOKE"))
    private void tick(Entity instance, Operation<Void> original) {
        if (!instance.getTags().contains("DoNotTick")) {
            original.call(instance);
        }
    }

    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickTime(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingTime || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTime) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickCustomSpawners",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickSpawners(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingSpawners || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingSpawners) {
            ci.cancel();
        }
    }

    @Inject(
            method = "runBlockEvents",
            at = @At("HEAD"),
            cancellable = true
    )
    private void processSyncedBlockEvents(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingBlockEvents || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingBlockEvents) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
            )
    )
    private void wrapRandomTick(BlockState instance, ServerLevel serverWorld, BlockPos blockPos, RandomSource random, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks) {
            original.call(instance, serverWorld, blockPos, random);
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    //#if MC < 12102
                    //$$ target = "Lnet/minecraft/world/level/material/FluidState;randomTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
                    //#else
                    target = "Lnet/minecraft/world/level/material/FluidState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
                    //#endif
            )
    )
    private void wrapFluidRandomTick(FluidState instance,
                                     //#if MC < 12102
                                     //$$ Level world,
                                     //#else
                                     ServerLevel world,
                                     //#endif
                                     BlockPos pos, RandomSource random, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingFluids || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids) {
            original.call(instance, world, pos, random);
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tickPrecipitation(Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void wrapIceAndSnowTick(ServerLevel instance, BlockPos pos, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks) {
            original.call(instance, pos);
        }
    }

    //#if MC >= 12105
    @Inject(method = "tickThunder", at = @At("HEAD"), cancellable = true)
    private void wrapLightningAndSkeletonHorseEntitySpawn(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionRules.stopTickingWeather) {
            ci.cancel();
        }
    }
    //#else
    //$$ @WrapOperation(
    //$$         method = "tickChunk",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
    //$$                 ordinal = 0
    //$$         )
    //$$ )
    //$$ private boolean wrapLightningSpawn(ServerLevel instance, Entity entity, Operation<Boolean> original) {
    //$$     if (!YetAnotherCarpetAdditionRules.stopTickingEntities || !YetAnotherCarpetAdditionRules.stopTickingWeather || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
    //$$         original.call(instance, entity);
    //$$     }
    //$$     return false;
    //$$ }
    //$$
    //$$ @WrapOperation(
    //$$         method = "tickChunk",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
    //$$                 ordinal = 1
    //$$         )
    //$$ )
    //$$ private boolean wrapSkeletonHorseEntitySpawn(ServerLevel instance, Entity entity, Operation<Boolean> original) {
    //$$     if (!YetAnotherCarpetAdditionRules.stopTickingWeather || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
    //$$         original.call(instance, entity);
    //$$     }
    //$$     return false;
    //$$ }
    //$$
    //#endif
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.globalOrder = 0;
        if (YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick ||
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks ||
                YetAnotherCarpetAdditionRules.stopTickingBlocks
        ) {
            blockTicks.allContainers.values().forEach(chunkTickScheduler -> {
                Queue<ScheduledTick<Block>> queuedTick = chunkTickScheduler.tickQueue;
                Queue<ScheduledTick<Block>> newQueuedTick = new PriorityQueue(ScheduledTick.DRAIN_ORDER);
                queuedTick.forEach(orderedTick -> {
                    newQueuedTick.add(new ScheduledTick<>(
                            orderedTick.type(),
                            orderedTick.pos(),
                            orderedTick.triggerTick() + 1,
                            orderedTick.priority(),
                            orderedTick.subTickOrder()
                    ));
                });
                if (newQueuedTick != null) {
                    chunkTickScheduler.tickQueue.clear();
                    chunkTickScheduler.tickQueue.addAll(newQueuedTick);
                }

            });
        }
        if (YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick ||
                YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids ||
                YetAnotherCarpetAdditionRules.stopTickingFluids
        ) {
            fluidTicks.allContainers.values().forEach(chunkTickScheduler -> {
                Queue<ScheduledTick<Fluid>> queuedTick = chunkTickScheduler.tickQueue;
                Queue<ScheduledTick<Fluid>> newQueuedTick = new PriorityQueue(ScheduledTick.DRAIN_ORDER);
                queuedTick.forEach(orderedTick -> {
                    newQueuedTick.add(new ScheduledTick<>(
                            orderedTick.type(),
                            orderedTick.pos(),
                            orderedTick.triggerTick() + 2,
                            orderedTick.priority(),
                            orderedTick.subTickOrder()
                    ));
                });
                if (newQueuedTick != null) {
                    chunkTickScheduler.tickQueue.clear();
                    chunkTickScheduler.tickQueue.addAll(newQueuedTick);
                }
            });
        }
    }
    @WrapOperation(
            method = "method_31420",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void entityTicking(ServerLevel instance, Consumer consumer, Entity entity, Operation<Void> original, @Local ProfilerFiller profiler) {
        if (!(YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities) || entity instanceof Player) {
            original.call(instance, consumer, entity);
        }
    }

    @WrapOperation(
            method = "method_31420",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;checkDespawn()V"
            )
    )
    private void entityDespawn(Entity instance, Operation<Void> original, @Local ProfilerFiller profiler) {
        if (!YetAnotherCarpetAdditionRules.stopCheckEntityDespawn || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopCheckEntityDespawn) {
            instance.checkDespawn();
        }
    }
}
