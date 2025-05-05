package mypals.ml.mixin.features.visualizers;

import mypals.ml.features.visualizingFeatures.HopperCooldownVisualizing;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperBlockEntity.class)
public class hopperEntityCooldownMixin {
    @Inject(
            method = "serverTick",
            at = @At("HEAD")
    )
    private static void ServerTickAddMarker(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld && YetAnotherCarpetAdditionRules.hopperCooldownVisualize) {
            HopperCooldownVisualizing.setVisualizer(serverWorld, pos, blockEntity.transferCooldown);
        }
    }
}
