package mypals.ml.mixin.features.visualizers;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public class WorldTickSchedulerRemoveMarkerMixin<T> {
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(target = "Ljava/util/List;add(Ljava/lang/Object;)Z", value = "INVOKE")
    )
    private void ServerTickAddScheduledTickMarker(BiConsumer<BlockPos, T> ticker, CallbackInfo ci, @Local OrderedTick<T> orderedTick) {
        if (YetAnotherCarpetAdditionRules.scheduledTickVisualize) {
            YetAnotherCarpetAdditionServer.scheduledTickVisualizing.removeVisualizer(orderedTick.pos());
        }
    }
}
