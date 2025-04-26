package mypals.ml.mixin.features.instantSchedule;

import mypals.ml.interfaces.WorldTickSchedule;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow @Final private net.minecraft.world.tick.WorldTickScheduler<Block> blockTickScheduler;

    @Shadow public abstract ServerWorld toServerWorld();

    @Shadow @Final private net.minecraft.world.tick.WorldTickScheduler<Fluid> fluidTickScheduler;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void setWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci){
        ((WorldTickSchedule) this.blockTickScheduler).setServerWorld(this.toServerWorld());
        ((WorldTickSchedule) this.fluidTickScheduler).setServerWorld(this.toServerWorld());
    }
}
