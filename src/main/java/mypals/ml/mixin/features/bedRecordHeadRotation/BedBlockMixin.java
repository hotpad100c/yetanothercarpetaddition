package mypals.ml.mixin.features.bedRecordHeadRotation;

import mypals.ml.interfaces.BedBlockEntityExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.BedBlock.PART;
import static net.minecraft.block.HorizontalFacingBlock.FACING;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {
    @Shadow
    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Inject(
            method = "onUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;"
            )
    )
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (YetAnotherCarpetAdditionRules.bedsRecordSleeperFacing) {
            if (world.getBlockEntity(pos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYaw());
                bedBlockEntityPlus.getSleeperPitch(player.getPitch());
            }
            BedPart bedPart = state.get(PART);
            BlockPos blockPos = pos.offset(getDirectionTowardsOtherPart(bedPart, state.get(FACING)));
            if (world.getBlockEntity(blockPos) instanceof BedBlockEntityExtension bedBlockEntityPlus) {
                bedBlockEntityPlus.setSleeperYaw(player.getYaw());
                bedBlockEntityPlus.getSleeperPitch(player.getPitch());
            }
        }
    }
}
