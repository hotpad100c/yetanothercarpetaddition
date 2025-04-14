package mypals.ml.mixin.features.optionalTicking;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.BooleanSupplier;

@Mixin(WorldBorder.class)
public class WorldBorderMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tick(CallbackInfo ci) {
        if(!YetAnotherCarpetAdditionRules.stopTickingWorldBorder) {
            ci.cancel();
        }
    }
}
