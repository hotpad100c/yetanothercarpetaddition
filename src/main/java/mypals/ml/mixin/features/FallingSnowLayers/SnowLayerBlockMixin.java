package mypals.ml.mixin.features.FallingSnowLayers;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SnowBlock.class)
public abstract class SnowLayerBlockMixin extends Block {
    public SnowLayerBlockMixin(Settings settings) {
        super(settings);
    }

    @WrapMethod(method = "getStateForNeighborUpdate")
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, Operation<BlockState> original) {
        if (YetAnotherCarpetAdditionRules.fallingSnowLayers
        ) {
            if (!state.canPlaceAt(world, pos)) {
                tickView.scheduleBlockTick(pos, world.getBlockState(pos).getBlock(), 2);
            }
            return super.getStateForNeighborUpdate(state,world,tickView, pos, direction, neighborPos,neighborState,random);
        } else {
            return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state,world,tickView, pos, direction, neighborPos,neighborState,random);

        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (YetAnotherCarpetAdditionRules.fallingSnowLayers
        ) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, pos, state);

        }
    }
}

