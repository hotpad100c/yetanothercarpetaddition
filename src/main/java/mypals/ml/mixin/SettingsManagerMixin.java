package mypals.ml.mixin;

import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpet.script.external.Carpet;
import carpet.utils.Messenger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_NAME;
import static mypals.ml.YetAnotherCarpetAdditionServer.MOD_VERSION;

@Mixin(SettingsManager.class)
public class SettingsManagerMixin {
	/*@Inject(method = "listAllSettings", slice = @Slice(from = @At(value = "CONSTANT", args = "this.version", ordinal = 0)),
			at = @At(value = "INVOKE", target = "Lcarpet/api/settings/SettingsManager;getCategories()Ljava/lang/Iterable;", ordinal = 0), remap = false)
	private void printAdditionVersion(ServerCommandSource source, CallbackInfoReturnable<Integer> cir) {
		Messenger.m(source, "g "+MOD_NAME+" version: " + MOD_VERSION);
	}*/
}