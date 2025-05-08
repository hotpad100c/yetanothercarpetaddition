package mypals.ml.mixin.features.kExplosion;

import mypals.ml.interfaces.ExplosionExtension;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Explosion.class)
public class ExplosionMixin implements ExplosionExtension {
    @Shadow @Final private World world;

    @Shadow @Final private @Nullable Entity entity;

    @Shadow @Final private Explosion.DestructionType destructionType;

    @Unique
    @Override
    public boolean preservesDecorativeEntities() {

        if(!YetAnotherCarpetAdditionRules.waterTNT)
            return true;

        boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        boolean bl2 = this.entity == null || !this.entity.isTouchingWater();
        boolean bl3 = this.entity == null || this.entity.getType() != EntityType.BREEZE_WIND_CHARGE && this.entity.getType() != EntityType.WIND_CHARGE;
        if (bl) {
            return bl2 && bl3;
        } else {
            return !(this.destructionType == Explosion.DestructionType.TRIGGER_BLOCK) && bl2 && bl3;
        }
    }
}
