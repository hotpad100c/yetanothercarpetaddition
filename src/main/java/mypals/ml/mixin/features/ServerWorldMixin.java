package mypals.ml.mixin.features;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World{
    @Shadow @Final private EntityList entityList;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow protected abstract boolean shouldCancelSpawn(Entity entity);

    @Shadow public abstract TickManager getTickManager();

    @Shadow @Final private ServerChunkManager chunkManager;

    @Shadow protected abstract void tickPassenger(Entity vehicle, Entity passenger);

    @Inject(
            method = "tickWeather",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickWeather(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingWeather) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickBlocks(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingBlocks) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickFluid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickFluid(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingFluids) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickEntity(Entity entity, CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingEntities && !(entity instanceof PlayerEntity)) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickTime",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickTime(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingTime) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickSpawners",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickSpawners(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingSpawners) {
            ci.cancel();
        }
    }
    @Inject(
            method = "processSyncedBlockEvents",
            at = @At("HEAD"),
            cancellable = true
    )
    private void processSyncedBlockEvents(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingBlockEvents) {
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
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks) {
            original.call(instance, serverWorld, blockPos, random);
        }
    }
    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;onRandomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V"
            )
    )
    private void wrapFluidRandomTick(FluidState instance, World world, BlockPos pos, Random random, Operation<Void> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingFluids) {
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
        if (!YetAnotherCarpetAdditionRules.stopTickingBlocks) {
            original.call(instance, pos);
        }
    }
    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    ordinal = 0
            )
    )
    private boolean wrapLightningSpawn(ServerWorld instance, Entity entity, Operation<Boolean> original) {
        if (!YetAnotherCarpetAdditionRules.stopTickingEntities || !YetAnotherCarpetAdditionRules.stopTickingWeather) {
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
        if (!YetAnotherCarpetAdditionRules.stopTickingWeather) {
            original.call(instance, entity);
        }
        return false;
    }
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void wrapEntityTicking(EntityList instance, Consumer<Entity> action, Operation<Void> original, @Local Profiler profiler) {
        this.entityList.forEach(entity -> {
            if (!entity.isRemoved()) {
                if (this.shouldCancelSpawn(entity)) {
                    entity.discard();
                } else if (!getTickManager().shouldSkipTick(entity)) {
                    profiler.push("checkDespawn");
                    if(!YetAnotherCarpetAdditionRules.stopCheckEntityDespawn){
                    entity.checkDespawn();
                    }
                    profiler.pop();
                    if (this.chunkManager.chunkLoadingManager.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
                        Entity entity2 = entity.getVehicle();
                        if (entity2 != null) {
                            if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
                                return;
                            }

                            entity.stopRiding();
                        }

                        profiler.push("tick");
                        this.tickEntity(this::tickEntity, entity);
                        profiler.pop();
                    }
                }
            }
        });
    }

    @Unique
    public void tickEntity(Entity entity) {
        if(YetAnotherCarpetAdditionRules.stopTickingEntities && !(entity instanceof PlayerEntity)) {
            return;
        }
        entity.resetPosition();
        Profiler profiler = this.getProfiler();
        entity.age++;
        this.getProfiler().push((Supplier<String>)(() -> Registries.ENTITY_TYPE.getId(entity.getType()).toString()));
        profiler.visit("tickNonPassenger");
        entity.tick();
        this.getProfiler().pop();

        for (Entity entity2 : entity.getPassengerList()) {
            this.tickPassenger(entity, entity2);
        }
    }

}
