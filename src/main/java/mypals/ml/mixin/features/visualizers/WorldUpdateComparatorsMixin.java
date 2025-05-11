package mypals.ml.mixin.features.visualizers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.BlockUpdateVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(World.class)
public abstract class WorldUpdateComparatorsMixin {
    @Shadow
    public abstract boolean isClient();

    @WrapOperation(
            method = "updateComparators",
            at = @At(target = "Lnet/minecraft/world/World;updateNeighbor" +
                    "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/" +
                    "math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/" +
                    "util/math/BlockPos;Z)V", ordinal = 0, value = "INVOKE")
    )
    private void AddCPMarkerSimple(World instance, BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.comparatorUpdateVisualize && !this.isClient())
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) this, pos, BlockUpdateVisualizing.UpdateType.CP);
        original.call(instance, state, pos, sourceBlock, sourcePos, notify);
    }

    @WrapOperation(
            method = "updateComparators",
            at = @At(target = "Lnet/minecraft/world/World;updateNeighbor" +
                    "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/" +
                    "math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/" +
                    "util/math/BlockPos;Z)V", ordinal = 1, value = "INVOKE")
    )
    private void AddCPMarkerThroughBlocks(World instance, BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.comparatorUpdateVisualize && !this.isClient())
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.setVisualizer((ServerWorld) (Object) this, pos, BlockUpdateVisualizing.UpdateType.CP);
        original.call(instance, state, pos, sourceBlock, sourcePos, notify);
    }
}
