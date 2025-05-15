package mypals.ml.mixin.features.morphMovingPiston;

import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {
//
//    @ModifyVariable(
//            method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/PistonBlockEntity;)V",
//            at = @At(
//                    value = "STORE",
//                    target = "Lnet/minecraft/block/piston/PistonBlockEntity;progress:F"
//            ),
//            ordinal = 0
//    )
//    private static float modifyPistonSpeed(float f, @Local(argsOnly = true) PistonBlockEntity blockEntity) {
//        return blockEntity.getProgress(0) + movingPistonSpeed;
//    }

}
