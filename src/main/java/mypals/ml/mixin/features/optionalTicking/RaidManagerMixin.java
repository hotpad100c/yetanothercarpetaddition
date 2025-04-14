package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RaidManager.class)
public class RaidManagerMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tick(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingRaid) {
            ci.cancel();
        }
    }
}
