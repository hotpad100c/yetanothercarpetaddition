package mypals.ml.mixin.features.rconOutputFix;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.settings.YetAnotherCarpetAdditionRules.RconOutputFix;

@Mixin(RconCommandOutput.class)
public abstract class rconOutputMixin implements CommandOutput {
    @Shadow
    @Final
    private StringBuffer buffer;

    @Inject(
            at = @At("TAIL"),
            method = "sendMessage"
    )
    private void onSendMessage(Text message, CallbackInfo ci) {
        if (RconOutputFix) {
            StringBuffer b = this.buffer;
            b.append(System.lineSeparator());
        }
    }
}
