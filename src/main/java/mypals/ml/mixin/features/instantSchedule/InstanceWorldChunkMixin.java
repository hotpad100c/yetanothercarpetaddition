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
