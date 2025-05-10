package mypals.ml.mixin.features;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(
            method = "onBlockAdded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickTime(CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "prepare",
            at = @At("HEAD"),
            cancellable = true
    )
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "getHardness",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getHardness(CallbackInfoReturnable<Float> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoHardness) {
            cir.setReturnValue(0f);
        }
    }


}
