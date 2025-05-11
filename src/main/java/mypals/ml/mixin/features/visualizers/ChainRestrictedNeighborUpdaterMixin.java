package mypals.ml.mixin.features.visualizers;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.features.visualizingFeatures.HopperCooldownVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class ChainRestrictedNeighborUpdaterMixin {
    @Shadow
    @Final
    private World world;

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerSimple(BlockPos pos, Block sourceBlock, BlockPos sourcePos, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.blockUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.NC);
    }

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerStateful(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.stateUpdateVisualize || this.world.isClient) return;
        YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos, BlockUpdateVisualizing.UpdateType.PP);
    }

    @Inject(
            method = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/Direction;)V",
            at = @At("HEAD")
    )
    private void AddNCMarkerSixWayEntry(BlockPos pos, Block sourceBlock, Direction except, CallbackInfo ci) {
        if (!YetAnotherCarpetAdditionRules.blockUpdateVisualize || this.world.isClient) return;
        for (Direction dir : NeighborUpdater.UPDATE_ORDER) {
            if (!(except != null && dir == except)) {
                YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) this.world, pos.offset(dir), BlockUpdateVisualizing.UpdateType.NC);
            }
        }

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
