package mypals.ml.mixin;

import carpet.api.settings.SettingsManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SettingsManager.class)
public class SettingsManagerMixin {
	/*@Inject(method = "listAllSettings", slice = @Slice(from = @At(value = "CONSTANT", args = "this.version", ordinal = 0)),
			at = @At(value = "INVOKE", target = "Lcarpet/api/settings/SettingsManager;getCategories()Ljava/lang/Iterable;", ordinal = 0), remap = false)
	private void printAdditionVersion(ServerCommandSource source, CallbackInfoReturnable<Integer> cir) {
		Messenger.m(source, "g "+MOD_NAME+" version: " + MOD_VERSION);
	}*/
}