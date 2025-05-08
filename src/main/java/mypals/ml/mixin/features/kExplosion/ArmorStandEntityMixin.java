package mypals.ml.mixin.features.kExplosion;

import mypals.ml.interfaces.ExplosionExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends Entity {
    @Shadow private boolean invisible;

    public ArmorStandEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return ((ExplosionExtension)explosion).preservesDecorativeEntities() ? this.invisible : true;
    }
}
