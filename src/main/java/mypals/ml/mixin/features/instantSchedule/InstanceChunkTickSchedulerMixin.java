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

