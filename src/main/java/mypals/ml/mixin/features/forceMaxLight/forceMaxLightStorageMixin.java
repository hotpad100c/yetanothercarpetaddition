package mypals.ml.mixin.features.forceMaxLight;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.world.chunk.light.LightStorage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightStorage.class)
public class forceMaxLightStorageMixin {
    @WrapMethod(method = "set")
    public void set(long blockPos, int value, Operation<Void> original) {
        original.call(blockPos, YetAnotherCarpetAdditionRules.forceMaxLightLevel ? 0 : value);
    }
}
