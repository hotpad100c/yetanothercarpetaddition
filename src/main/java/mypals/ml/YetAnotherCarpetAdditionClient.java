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
import mypals.ml.features.selectiveFreeze.SelectiveFreezeManager;
import mypals.ml.network.OptionalFreezePayload;
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
//#if MC < 12006
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//$$ import net.minecraft.network.PacketByteBuf;
//#endif

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
    public static SelectiveFreezeManager selectiveFreezeManager = new SelectiveFreezeManager();
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
                String lang = client.getLanguageManager().getLanguage();
                //#if MC >= 12006
                ClientPlayNetworking.send(new RequestRulesPayload(lang));
                //#else
                //$$ PacketByteBuf buf = PacketByteBufs.create();
                //$$ ClientPlayNetworking.send(RequestRulesPayload.ID, buf.writeString(lang));
                //#endif
                requesting = true;
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(OptionalFreezePayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.client().execute(() -> {
                //#else
                //$$ (client, handler, buf, responseSender) -> client.execute(() -> {
                //$$     OptionalFreezePayload payload = new OptionalFreezePayload(buf);
                //#endif
                    switch (payload.phase().toLowerCase()) {
                        case "worldborder":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingWorldBorder = payload.freeze();
                            break;
                        case "weather":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingWeather = payload.freeze();
                            break;
                        case "time":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTime = payload.freeze();
                            break;
                        case "tileblocks":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileBlocks = payload.freeze();
                            break;
                        case "tilefluids":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileFluids = payload.freeze();
                            break;
                        case "tiletick":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingTileTick = payload.freeze();
                            break;
                        case "raid":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingRaid = payload.freeze();
                            break;
                        case "chunkmanager":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingChunkManager = payload.freeze();
                            break;
                        case "blockevents":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingBlockEvents = payload.freeze();
                            break;
                        case "dragonfight":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingDragonFight = payload.freeze();
                            break;
                        case "entitydespawn":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopCheckEntityDespawn = payload.freeze();
                            break;
                        case "entities":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingEntities = payload.freeze();
                            break;
                        case "blockentities":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingBlockEntities = payload.freeze();
                            break;
                        case "spawners":
                            YetAnotherCarpetAdditionClient.selectiveFreezeManager.stopTickingSpawners = payload.freeze();
                            break;
                        default:

                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(RulesPacketPayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.client().execute(() -> {
                    MinecraftClient client = context.client();
                //#else
                //$$ (client, player, buf, packetSender) -> client.execute(() -> {
                //$$   RulesPacketPayload payload = new RulesPacketPayload(buf);
                //#endif
                    chachedRules.clear();
                    chachedRules.addAll(payload.rules());

                    chachedCategories.clear();
                    chachedCategories.add("favorite");
                    chachedCategories.add("default");
                    chachedCategories.addAll(chachedRules.stream()
                            .flatMap(r -> r.categories.stream())
                            .distinct().toList());
                    client.player.sendMessage(Text.literal("Received " + chachedRules.size() + " rules from server！")
                            //#if MC >= 12102
                            //$$ , false
                            //#endif
                    );
                    requesting = false;
                    defaultRules.clear();
                    defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());

                    favoriteRules.clear();
                    favoriteRules.addAll(YACAConfigManager.readFavoriteRules());

                    client.setScreen(new RulesEditScreen(Text.of("Carpet Rules")));
                }));
        ClientPlayNetworking.registerGlobalReceiver(CountersPacketPayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.client().execute(() -> {
                    MinecraftClient client = context.client();
                //#else
                //$$ (client, player, buf, packetSender) -> client.execute(() -> {
                //$$   CountersPacketPayload payload = new CountersPacketPayload(buf);
                //#endif
                    client.setScreen(new CounterViewerScreen(payload.currentRecords()));
                }));
        ClientCommandRegistrationCallback.EVENT.register(HopperCounterRequestCommand::registerCommand);

    }
}