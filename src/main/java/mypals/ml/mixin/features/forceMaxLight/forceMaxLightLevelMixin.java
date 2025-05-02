package mypals.ml.mixin.features.forceMaxLight;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkLightProvider.class)
public abstract class forceMaxLightLevelMixin {
    @Shadow
    @Final
    private LongOpenHashSet blockPositionsToCheck;

    @Shadow
    @Final
    private LongArrayFIFOQueue field_44734;

    @Shadow
    @Final
    private LongArrayFIFOQueue field_44735;

    @Shadow
    protected abstract void clearChunkCache();

    @WrapMethod(method = "getLightLevel")
    private int getLightLevel(BlockPos pos, Operation<Integer> original) {
        return YetAnotherCarpetAdditionRules.forceMaxLightLevel ? (int) (15L) : original.call(pos);
    }

    @Inject(
            method = "checkBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldCheckBlock(BlockPos pos, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.disableLightUpdate) {
            ci.cancel();
        }
    }

}
