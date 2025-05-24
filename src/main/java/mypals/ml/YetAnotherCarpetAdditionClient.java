/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml;

import mypals.ml.Screen.CountersViewer.CounterViewerScreen;
import mypals.ml.Screen.RulesEditScreen.RulesEditScreen;
import mypals.ml.commands.HopperCounterRequestCommand;
import mypals.ml.network.RuleData;
import mypals.ml.network.client.RequestRulesPayload;
import mypals.ml.network.server.CountersPacketPayload;
import mypals.ml.network.server.RulesPacketPayload;
import mypals.ml.settings.YACAConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
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
import java.util.concurrent.CopyOnWriteArrayList;

public class YetAnotherCarpetAdditionClient implements ClientModInitializer {
    public static KeyBinding carpetRulesKeyBind;
    public static List<RuleData> chachedRules = new ArrayList<>();
    public static List<String> chachedCategories = new ArrayList<>();
    public static CopyOnWriteArrayList<String> defaultRules = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<String> favoriteRules = new CopyOnWriteArrayList<>();
    public boolean requesting = false;

    @Override
    public void onInitializeClient() {
        YACAConfigManager.initializeConfig();
        carpetRulesKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.carpetRulesKeyBind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.MAIN"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (carpetRulesKeyBind.wasPressed()) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("Requesting rules now！")
                        //#if MC >= 12102
                        //$$ , false
                        //#endif
                );
                ClientPlayNetworking.send(new RequestRulesPayload(MinecraftClient.getInstance().getLanguageManager().getLanguage()));
                requesting = true;
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(RulesPacketPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                chachedRules.clear();
                chachedRules.addAll(payload.rules());

                chachedCategories.clear();
                chachedCategories.add("favorite");
                chachedCategories.add("default");
                chachedCategories.addAll(chachedRules.stream()
                        .flatMap(r -> r.categories.stream())
                        .distinct().toList());

                context.client().player.sendMessage(Text.literal("Received " + chachedRules.size() + " rules from server！")
                        //#if MC >= 12102
                        //$$ , false
                        //#endif
                );
                requesting = false;
                defaultRules.clear();
                defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());

                favoriteRules.clear();
                favoriteRules.addAll(YACAConfigManager.readFavoriteRules());

                MinecraftClient.getInstance().setScreen(new RulesEditScreen(Text.of("Carpet Rules")));
            });
        });
        ClientCommandRegistrationCallback.EVENT.register(HopperCounterRequestCommand::registerCommand);
        ClientPlayNetworking.registerGlobalReceiver(CountersPacketPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient.getInstance().setScreen(new CounterViewerScreen(payload.currentRecords()));
            });
        });
    }
}