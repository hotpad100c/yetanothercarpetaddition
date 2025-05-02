package mypals.ml.mixin.features.Farlands;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({OctavePerlinNoiseSampler.class})
public class FarlandsPerlinSimplexNoiseMixin {
    @Inject(
            method = {"maintainPrecision"},
            at = {@At("TAIL")},
            cancellable = true
    )
    private static void maintainPrecision(double value, CallbackInfoReturnable<Double> cir) {
        if (YetAnotherCarpetAdditionRules.farlandReintroduced) {
            cir.setReturnValue(value);
        }

    }
}
