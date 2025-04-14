package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickEntity(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingDragonFight) {
            ci.cancel();
        }
    }
}
