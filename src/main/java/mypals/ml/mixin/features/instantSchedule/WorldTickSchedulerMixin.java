package mypals.ml.mixin.features.instantSchedule;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mypals.ml.interfaces.WorldTickSchedule;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.*;

@Mixin(WorldTickScheduler.class)
public abstract class WorldTickSchedulerMixin<T> implements WorldTickSchedule {

    @Unique
    ServerWorld serverWorld;

    @Shadow
    @Final
    private Long2ObjectMap<ChunkTickScheduler<T>> chunkTickSchedulers;

    /**
     * @author AB
     * @reason w?
     */
    @WrapMethod(method = "scheduleTick")
    public void scheduleTick(OrderedTick<T> orderedTick, Operation<Void> original) {
        long l = ChunkPos.toLong(orderedTick.pos());
        ChunkTickScheduler<T> chunkTickScheduler = this.chunkTickSchedulers.get(l);
        if (chunkTickScheduler == null) {
            Util.throwOrPause(new IllegalStateException("Trying to schedule tick in not loaded position " + orderedTick.pos()));
        } else {
            if (YetAnotherCarpetAdditionRules.instantSchedule) {
                if (orderedTick.type() instanceof Block)
                    serverWorld.tickBlock(orderedTick.pos(), (Block) orderedTick.type());
                else
                    serverWorld.tickFluid(orderedTick.pos(), (Fluid) orderedTick.type());
            } else {
                chunkTickScheduler.scheduleTick(orderedTick);
            }
        }
    }


    @Unique
    @Override
    public void setServerWorld(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }
}
