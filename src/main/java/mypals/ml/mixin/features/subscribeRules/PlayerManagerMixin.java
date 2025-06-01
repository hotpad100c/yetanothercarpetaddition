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

package mypals.ml.mixin.features.subscribeRules;

import mypals.ml.features.subscribeRules.RuleSubscribeManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    public abstract void sendToAll(Packet<?> packet);

    @Inject(at = @At("HEAD"), method = "updatePlayerLatency")
    public void updatePlayerLatency(CallbackInfo ci) {
        MutableText multitext = Text.empty();
        RuleSubscribeManager.subscribed.forEach((name, rule) -> {
            Formatting valColor = rule.value() == rule.defaultValue() ? Formatting.GRAY : Formatting.GOLD;
            multitext.append(name).append(Text.literal(rule.value() + "\n").formatted(valColor));
        });
        if (!RuleSubscribeManager.subscribed.isEmpty())
            this.sendToAll(new PlayerListHeaderS2CPacket(
                    Text.empty(),
                    multitext
            ));
    }

    /*@Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        if (
                Config.INSTANCE.enabled
                        && Config.INSTANCE.motd != null // check if set in config file
                        && !Config.INSTANCE.motd.isEmpty())
            player.sendMessage(
                    Text.literal(Config.INSTANCE.motd), false);
    }*/
}
