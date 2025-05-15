package mypals.ml.mixin.features.visualizers;

import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HopperBlock.class)
public abstract class HopperDeleteMarkerMixin extends BlockWithEntity {
    protected HopperDeleteMarkerMixin(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (YetAnotherCarpetAdditionRules.hopperCooldownVisualize) {
            YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.removeVisualizer(pos);
        }
        return super.onBreak(world, pos, state, player);
    }
}
