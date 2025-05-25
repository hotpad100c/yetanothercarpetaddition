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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.TickManager;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC >= 12102
//$$ import net.minecraft.util.profiler.Profilers;
//#endif

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Shadow
    @Final
    private EntityList entityList;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry,
                //#if MC <12102
                profiler,
                //#endif
                isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    //#if MC < 12102
    @Shadow
    protected abstract boolean shouldCancelSpawn(Entity entity);
    //#endif

    @Shadow
    public abstract TickManager getTickManager();

    @Shadow
    @Final
    private ServerChunkManager chunkManager;

    @Shadow
    protected abstract void tickPassenger(Entity vehicle, Entity passenger);

    @Shadow
    @Final
    private WorldTickScheduler<Fluid> fluidTickScheduler;

    @Shadow
    @Final
    private WorldTickScheduler<Block> blockTickScheduler;

    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Inject(
            method = "tickWeather",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickWeather(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingWeather || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V",
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
            method = "tickEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickEntity(Entity entity, CallbackInfo ci) {
        if ((YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities) && !(entity instanceof PlayerEntity)) {
            ci.cancel();
        }
        if (entity.getCommandTags().contains("DoNotTick")) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "tickEntity",
            at = @At(target = "Lnet/minecraft/entity/Entity;tick()V", value = "INVOKE"))
    private void tick(Entity instance, Operation<Void> original) {
        if (!instance.getCommandTags().contains("DoNotTick")) {
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
            method = "tickSpawners",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickSpawners(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.stopTickingSpawners || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingSpawners) {
            ci.cancel();
        }
    }

    @Inject(
            method = "processSyncedBlockEvents",
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
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void wrapRandomTick(BlockState instance, ServerWorld serverWorld, BlockPos blockPos, Random random, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks) {
            original.call(instance, serverWorld, blockPos, random);
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    //#if MC < 12102
                    target = "Lnet/minecraft/fluid/FluidState;onRandomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
                    //#else
                    //$$ target = "Lnet/minecraft/fluid/FluidState;onRandomTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
                    //#endif
            )
    )
    private void wrapFluidRandomTick(FluidState instance,
                                     //#if MC < 12102
                                     World world,
                                     //#else
                                     //$$ ServerWorld world,
                                     //#endif
                                     BlockPos pos, Random random, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingFluids || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids) {
            original.call(instance, world, pos, random);
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickIceAndSnow(Lnet/minecraft/util/math/BlockPos;)V"
            )
    )
    private void wrapIceAndSnowTick(ServerWorld instance, BlockPos pos, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks) {
            original.call(instance, pos);
        }
    }

    //#if MC >= 12105
    //$$ @Inject(method = "tickThunder", at = @At("HEAD"), cancellable = true)
    //$$ private void wrapLightningAndSkeletonHorseEntitySpawn(CallbackInfo ci) {
    //$$     if (YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionRules.stopTickingWeather) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#else
    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    ordinal = 0
            )
    )
    private boolean wrapLightningSpawn(ServerWorld instance, Entity entity, Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingEntities || !YetAnotherCarpetAdditionRules.stopTickingWeather || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
            original.call(instance, entity);
        }
        return false;
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    ordinal = 1
            )
    )
    private boolean wrapSkeletonHorseEntitySpawn(ServerWorld instance, Entity entity, Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingWeather || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingWeather) {
            original.call(instance, entity);
        }
        return false;
    }

    //#endif
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        YetAnotherCarpetAdditionServer.blockEntityOrderVisualizing.globalOrder = 0;
        if (YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileBlocks || YetAnotherCarpetAdditionRules.stopTickingBlocks) {
            blockTickScheduler.chunkTickSchedulers.values().forEach(chunkTickScheduler -> {
                Queue<OrderedTick<Block>> queuedTick = chunkTickScheduler.tickQueue;
                Queue<OrderedTick<Block>> newQueuedTick = new PriorityQueue(OrderedTick.TRIGGER_TICK_COMPARATOR);
                queuedTick.forEach(orderedTick -> {
                    newQueuedTick.add(new OrderedTick(
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
        if (YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileTick || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingTileFluids || YetAnotherCarpetAdditionRules.stopTickingFluids) {
            fluidTickScheduler.chunkTickSchedulers.values().forEach(chunkTickScheduler -> {
                Queue<OrderedTick<Fluid>> queuedTick = chunkTickScheduler.tickQueue;
                Queue<OrderedTick<Fluid>> newQueuedTick = new PriorityQueue(OrderedTick.TRIGGER_TICK_COMPARATOR);
                queuedTick.forEach(orderedTick -> {
                    newQueuedTick.add(new OrderedTick(
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

    private static float targetAInterval = 1000.0f / SelectiveFreezeManager.entitiesTickSpeed;
    private static float accumulatedTime = 0.0f;
    private static long lastTickTime = System.nanoTime();

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void wrapEntityTicking(EntityList instance, Consumer<Entity> action, Operation<Void> original, @Local Profiler profiler) {
        /*long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTickTime) / 1_000_000.0f;
        accumulatedTime += deltaTime;
        lastTickTime = currentTime;
        targetAInterval = 1000.0f / SelectiveFreezeManager.entitiesTickSpeed;
        if (SelectiveFreezeManager.entitiesTickSpeed <= this.getServer().getTickManager().getTickRate()) {
            if (accumulatedTime >= targetAInterval) {
                entityTicking(instance, action, original, profiler);
                accumulatedTime -= targetAInterval;
            }
        } else {
            int ticks = (int) (accumulatedTime / targetAInterval);
            for (int i = 0; i < ticks; i++) {
                entityTicking(instance, action, original, profiler);
            }
            accumulatedTime -= ticks * targetAInterval;
        }*/
        original.call(instance, action);
        //entityTicking(instance, action, original, profiler);
    }

    @WrapOperation(
            method = "method_31420",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickEntity(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void entityTicking(ServerWorld instance, Consumer consumer, Entity entity, Operation<Void> original, @Local Profiler profiler) {
        if (!(YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities) || entity instanceof PlayerEntity) {
            original.call(instance, consumer, entity);
        }
    }

    @WrapOperation(
            method = "method_31420",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;checkDespawn()V"
            )
    )
    private void entityDespawn(Entity instance, Operation<Void> original, @Local Profiler profiler) {
        if (!YetAnotherCarpetAdditionRules.stopCheckEntityDespawn || !YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopCheckEntityDespawn) {
            instance.checkDespawn();
        }
    }

    @Unique
    public void tickEntity(Entity entity) {
        if ((YetAnotherCarpetAdditionRules.stopTickingEntities || YetAnotherCarpetAdditionServer.selectiveFreezeManager.stopTickingEntities) && !(entity instanceof PlayerEntity)) {
            return;
        }
        entity.resetPosition();
        Profiler profiler =
                //#if MC < 12102
                this.getProfiler();
        //#else
        //$$ Profilers.get();
        //#endif
        entity.age++;
        profiler.push((Supplier<String>) (() -> Registries.ENTITY_TYPE.getId(entity.getType()).toString()));
        profiler.visit("tickNonPassenger");
        entity.tick();
        profiler.pop();

        for (Entity entity2 : entity.getPassengerList()) {
            this.tickPassenger(entity, entity2);
        }
    }

}
