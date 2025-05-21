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
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixinForCopyableChatMessage {
    @Shadow
    public ServerPlayerEntity player;

    @WrapMethod(
            method = "handleDecoratedMessage"
    )
    private void modifyDecoratedMessage(SignedMessage message, Operation<Void> original) {
        if (YetAnotherCarpetAdditionRules.copyablePlayerMessages) {

            Style hoverStyle = Style.EMPTY
                    .withFormatting(Formatting.UNDERLINE)
                    .withColor(Formatting.LIGHT_PURPLE);
            Text hoverText = Text.literal("Click to copy").formatted(Formatting.ITALIC).setStyle(hoverStyle);

            Text modifiedMessage = Text.of(message.getSignedContent()).copy().setStyle(Style.EMPTY
                    .withClickEvent(ClickEvent.copyToClipboard(message.getSignedContent()))
                    .withHoverEvent(HoverEvent.showText(hoverText))
            );

            SignedMessage copyableSignedMessage = new SignedMessage(
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
