package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class WorldMixin {
    @Inject(
            method = "tickBlockEntities",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickBlockEntities(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingBlockEntities) {
            ci.cancel();
        }
    }
}
