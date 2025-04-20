package mypals.ml;

import mypals.ml.Screen.RulesEditScreen;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.network.server.RulesPacketPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class YetAnotherCarpetAdditionClient implements ClientModInitializer {
    public static KeyBinding carpetRulesKeyBind;
    public static List<RuleData> chachedRules = new ArrayList<>();
    public static List<String> chachedCategories = new ArrayList<>();
    public static List<String> defaultRules = new ArrayList<>();
    public boolean requesting = false;

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
                ClientPlayNetworking.send(new RequestRulesPayload(MinecraftClient.getInstance().getLanguageManager().getLanguage()));
                requesting = true;
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(RulesPacketPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                chachedRules.clear();
                chachedRules.addAll(payload.rules());
                chachedCategories = chachedRules.stream()
                        .flatMap(r -> r.categories.stream())
                        .distinct().toList();
                context.client().player.sendMessage(Text.literal("Received " + chachedRules.size() + " rules from server！"));
                requesting = false;
                defaultRules = Arrays.stream(payload.defaults().split(";")).toList();
                MinecraftClient.getInstance().setScreen(new RulesEditScreen(Text.of("Carpet Rules")));
            });
        });
    }
}