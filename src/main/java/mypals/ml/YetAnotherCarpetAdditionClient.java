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

import mypals.ml.screen.countersViewerScreen.CounterViewerScreen;
import mypals.ml.screen.rulesEditScreen.RulesEditScreen;
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
//#if MC >= 260100
//$$ import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//#else
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//#endif
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//#if MC <= 12004
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//$$ import net.minecraft.network.FriendlyByteBuf;
//#endif

public class YetAnotherCarpetAdditionClient implements ClientModInitializer {
    public static KeyMapping carpetRulesKeyBind;
    public static List<RuleData> chachedRules = new ArrayList<>();
    public static List<String> chachedCategories = new ArrayList<>();
    public static CopyOnWriteArrayList<String> defaultRules = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<String> favoriteRules = new CopyOnWriteArrayList<>();
    public static SelectiveFreezeManager selectiveFreezeManager = new SelectiveFreezeManager();
    public boolean requesting = false;
    //#if MC >= 12109
    private static final KeyMapping.Category YACA_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yaca", "name"));
    //#endif

    @Override
    @SuppressWarnings("resource")
    public void onInitializeClient() {
        YACAConfigManager.initializeConfig();
        carpetRulesKeyBind =
                //#if MC >= 260100
                //$$ KeyMappingHelper.registerKeyMapping(new KeyMapping(
                //#else
                KeyBindingHelper.registerKeyBinding(new KeyMapping(
                //#endif
                "key.carpetRulesKeyBind",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                //#if MC >= 12109
                YACA_CATEGORY
                //#else
                //$$ "key.category.yaca.name"
                //#endif
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (carpetRulesKeyBind.consumeClick()) {
                String lang = client.getLanguageManager().getSelected();
                //#if MC >= 12006
                ClientPlayNetworking.send(new RequestRulesPayload(lang));
                //#else
                //$$ FriendlyByteBuf buf = PacketByteBufs.create();
                //$$ ClientPlayNetworking.send(RequestRulesPayload.ID, buf.writeUtf(lang));
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
                    Minecraft client = context.client();
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
                    requesting = false;
                    defaultRules.clear();
                    defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());

                    favoriteRules.clear();
                    favoriteRules.addAll(YACAConfigManager.readFavoriteRules());

                    //#if MC >= 260100
                    //$$ client.setScreenAndShow(new RulesEditScreen(Component.nullToEmpty("Carpet Rules")));
                    //#else
                    client.setScreen(new RulesEditScreen(Component.nullToEmpty("Carpet Rules")));
                    //#endif
                }));
        ClientPlayNetworking.registerGlobalReceiver(CountersPacketPayload.ID,
                //#if MC >= 12006
                (payload, context) -> context.client().execute(() -> {
                    Minecraft client = context.client();
                //#else
                //$$ (client, player, buf, packetSender) -> client.execute(() -> {
                //$$   CountersPacketPayload payload = new CountersPacketPayload(buf);
                //#endif
                    //#if MC >= 260100
                    //$$ client.setScreenAndShow(new CounterViewerScreen(payload.currentRecords()));
                    //#else
                    client.setScreen(new CounterViewerScreen(payload.currentRecords()));
                    //#endif
                }));
        ClientCommandRegistrationCallback.EVENT.register(HopperCounterRequestCommand::registerCommand);

    }
}