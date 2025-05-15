package mypals.ml.mixin.features.instantFalling;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {
    
    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!FallingBlock.canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY()) {
            return;
        }
        if (YetAnotherCarpetAdditionRules.instantFalling) {
            BlockState blockState = world.getBlockState(pos);
            world.removeBlock(pos, false);
            BlockPos blockPos = pos.down();
            while (FallingBlock.canFallThrough(world.getBlockState(blockPos)) && blockPos.getY() > world.getBottomY()) {
                blockPos = blockPos.down();
            }
            if (blockPos.getY() >= world.getBottomY()) {
                world.setBlockState(blockPos.up(), blockState);
            }
            ci.cancel();
        }
    }
}
