package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.world.ServerEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityManager.class)
public class ServerChunkManagerMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickChunkManager(CallbackInfo ci) {
        if(YetAnotherCarpetAdditionRules.stopTickingChunkManager) {
            ci.cancel();
        }
    }

}
