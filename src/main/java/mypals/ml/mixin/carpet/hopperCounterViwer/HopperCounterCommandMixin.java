package mypals.ml.mixin.carpet.hopperCounterViwer;

import carpet.commands.CounterCommand;
import mypals.ml.features.hopperCounterDataCollector.HopperCounterDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(CounterCommand.class)
public class HopperCounterCommandMixin {
    @Inject(
            method = "resetCounter",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void resetCounter(CallbackInfoReturnable<Integer> cir) throws IOException {
        try {
            HopperCounterDataManager.getCounterLogger().clearCounters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(
            method = "resetCounters",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void resetCounters(CallbackInfoReturnable<Integer> cir) throws IOException {
        try {
            HopperCounterDataManager.getCounterLogger().clearCounters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
