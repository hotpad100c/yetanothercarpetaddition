package mypals.ml.mixin.features.visualizers;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.block.SimpleNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleNeighborUpdater.class)
public class SimpleNeighborUpdaterMixin {
    @Shadow
    @Final
    private World world;

    @Inject(
            //#if MC < 12102
            method = "Lnet/minecraft/world/block/SimpleNeighborUpdater;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V",
            //#else
            //$$ method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V",
            //#endif
            at = @At("HEAD")
    )
    private void AddNCMarkerSimple(BlockPos pos, Block sourceBlock,
                                   //#if MC < 12102
                                   BlockPos sourcePos,
                                   //#else
                                   //$$ WireOrientation orientation,
                                   //#endif
                                   CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.blockUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.NC);
    }

    @Inject(
            //#if MC < 12102
            method = "Lnet/minecraft/world/block/SimpleNeighborUpdater;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
            //#else
            //$$ method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V",
            //#endif
            at = @At("HEAD")
    )
    private void AddNCMarkerStateful(BlockState state, BlockPos pos, Block sourceBlock,
                                     //#if MC < 12102
                                     BlockPos sourcePos,
                                     //#else
                                     //$$ WireOrientation orientation,
                                     //#endif
                                     boolean notify, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.PP);
    }
    

    @Inject(
            method = "replaceWithStateForNeighborUpdate",
            at = @At("HEAD")
    )
    private void AddNCMarker(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        //if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || this.world.isClient) return;
        //YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.PP);

    }
}
