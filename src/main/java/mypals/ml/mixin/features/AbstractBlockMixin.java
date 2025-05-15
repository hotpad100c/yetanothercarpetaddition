package mypals.ml.mixin.features;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
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
            method = "getStateForNeighborUpdate",
            at = @At("HEAD")
    )
    private void AddPPMarker(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || world.isClient()) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) world, pos, BlockUpdateVisualizing.UpdateType.PP);

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
