package mypals.ml.mixin.features.optionalTicking;

import net.minecraft.server.world.ServerEntityManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {
    /*@Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tick(CallbackInfo ci) {
        if(!YetAnotherCarpetAdditionRules.stopTickingEntities) {
            ci.cancel();
        }
    }*/
}
