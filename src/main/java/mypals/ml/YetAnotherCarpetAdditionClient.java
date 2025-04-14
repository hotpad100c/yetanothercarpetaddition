package mypals.ml;

import carpet.CarpetServer;
import carpet.network.CarpetClient;
import carpet.network.ClientNetworkHandler;
import mypals.ml.network.client.RequestRulesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YetAnotherCarpetAdditionClient implements ClientModInitializer {
	public static KeyBinding carpetRulesKeyBind;

	@Override
	public void onInitializeClient() {
		carpetRulesKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.carpetRulesKeyBind",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F8,
				"category.MAIN"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (carpetRulesKeyBind.wasPressed()) {
				MinecraftClient.getInstance().player.sendMessage(Text.literal("Requesting rules now！"));
				ClientPlayNetworking.send(new RequestRulesPayload());
			}
		});
	}
}