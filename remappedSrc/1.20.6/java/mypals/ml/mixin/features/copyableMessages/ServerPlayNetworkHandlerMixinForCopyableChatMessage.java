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

package mypals.ml.mixin.features.copyableMessages;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.ClickEvent;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixinForCopyableChatMessage {
    @Shadow
    public ServerPlayer player;

    @WrapMethod(
            method = "broadcastChatMessage"
    )
    private void modifyDecoratedMessage(PlayerChatMessage message, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.copyablePlayerMessages) {

            Style hoverStyle = Style.EMPTY
                    .applyFormat(ChatFormatting.UNDERLINE)
                    .withColor(ChatFormatting.LIGHT_PURPLE);
            Component hoverText = Component.literal("Click to copy").withStyle(ChatFormatting.ITALIC).setStyle(hoverStyle);

            Component modifiedMessage = Component.nullToEmpty(message.signedContent()).copy().setStyle(Style.EMPTY
                    .withClickEvent(ClickEvent.copyToClipboard(message.signedContent()))
                    .withHoverEvent(HoverEvent.showText(hoverText))
            );

            PlayerChatMessage copyableSignedMessage = new PlayerChatMessage(
                    message.link(),
                    message.signature(),
                    message.signedBody(),
                    modifiedMessage,
                    message.filterMask()
            );
            original.call(copyableSignedMessage);
        } else {
            original.call(message);
        }
    }
}
