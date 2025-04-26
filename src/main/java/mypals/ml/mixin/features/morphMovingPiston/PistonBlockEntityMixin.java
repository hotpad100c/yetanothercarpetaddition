package mypals.ml.mixin.features.morphMovingPiston;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.movingPistonSpeed;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {

    @ModifyVariable(
            method = "tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/PistonBlockEntity;)V",
            at = @At(
                    value = "STORE",
                    target = "Lnet/minecraft/block/piston/PistonBlockEntity;progress:F"
            ),
            ordinal = 0
    )
    private static float modifyPistonSpeed(float f, @Local(argsOnly = true) PistonBlockEntity blockEntity) {

        return blockEntity.getProgress(0) + movingPistonSpeed;
    }

}
