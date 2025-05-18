package mypals.ml.mixin.features;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(
            method = "onBlockAdded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBlockAdded(World world, BlockPos pos, BlockState state, boolean notify, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "prepare",
            at = @At("HEAD"),
            cancellable = true
    )
    public void prepare(WorldAccess world, BlockPos pos, int flags, CallbackInfo ci) {
        if (YetAnotherCarpetAdditionRules.blocksNoSelfCheck) {
            ci.cancel();
        }
    }

    @Inject(
            method = "shouldSuffocate",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onStateReplaced(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoSuffocate) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "hasBlockBreakParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    public void hasBlockBreakParticles(CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blockNoBreakParticles) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "getHardness",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (YetAnotherCarpetAdditionRules.blocksNoHardness) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(
            method = "canPlaceAt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void canPlaceAt(CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.blocksPlaceAtAnywhere) {
            cir.setReturnValue(true);
        }
    }

}
