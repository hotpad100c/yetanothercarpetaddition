package mypals.ml.mixin.features.visualizers;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class PostProcessStateBlockMixin {
    @Inject(
            method = "postProcessState",
            at = @At(target = "Lnet/minecraft/block/BlockState;getStateForNeighborUpdate(Lnet/minecraft/world/WorldView;Lnet/minecraft/world/tick/ScheduledTickView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/block/BlockState;", ordinal = 0, value = "INVOKE")
    )
    private static void AddPPMarker(BlockState state, WorldAccess world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || world.isClient()) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) world, pos, BlockUpdateVisualizing.UpdateType.PP);

    }

}
