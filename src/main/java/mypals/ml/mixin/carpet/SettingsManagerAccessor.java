package mypals.ml.mixin.carpet;

import carpet.api.settings.SettingsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.Map;

@Mixin(SettingsManager.class)
public interface SettingsManagerAccessor {

}
