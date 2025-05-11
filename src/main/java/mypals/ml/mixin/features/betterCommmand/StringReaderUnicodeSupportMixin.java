package mypals.ml.mixin.features.betterCommmand;

import com.mojang.brigadier.StringReader;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringReader.class)
public class StringReaderUnicodeSupportMixin {
    @Inject(method = "isAllowedInUnquotedString", at = @At("RETURN"), remap = false, cancellable = true)
    private static void onIsAllowedInUnquotedString(char c, CallbackInfoReturnable<Boolean> cir) {
        if (YetAnotherCarpetAdditionRules.unicodeArgumentsSupport) {
            cir.setReturnValue(cir.getReturnValueZ() || Character.isLetterOrDigit(c) || c > 0x7F); // 0x7F 是 ASCII 范围的结束符
        }
    }
}
