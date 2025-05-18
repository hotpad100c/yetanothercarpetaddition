package mypals.ml.mixin.features;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin extends TranslucentBlock {
    public SlimeBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!YetAnotherCarpetAdditionRules.bouncierSlime) return;
        if (!entity.bypassesLandingEffects()) bounceSide(entity);
    }

    @Unique
    private void bounceSide(Entity entity) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.y < 0.0 || vec3d.x < 0.0 || vec3d.z < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(-vec3d.x * d, -vec3d.y * d, -vec3d.z * d);
        }
    }
}
