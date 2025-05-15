package mypals.ml.mixin.features.forceMaxLight;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkSkyLightProvider.class)
public class disableSkyLightProviderMixin {
    @Inject(
            method = "method_51529",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates1(long blockPos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "method_51530",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates2(long blockPos, long l, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "method_51531",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldDoLightUpdates3(long blockPos, long l, int lightLevel, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    @Inject(
            method = "propagateLight",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldPropagateLight(ChunkPos chunkPos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

    /*@Inject(
            method = "setColumnEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldSetColumnEnabled(ChunkPos pos, boolean retainData, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }*/
}
