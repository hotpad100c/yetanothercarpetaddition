package mypals.ml.mixin.features;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    /*@Inject(
            method = "getMaxLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
       /* if(YetAnotherCarpetAdditionRules.enchantCommandLimitOverwrite) {
            cir.setReturnValue(32767);
        }
    }*/
}
