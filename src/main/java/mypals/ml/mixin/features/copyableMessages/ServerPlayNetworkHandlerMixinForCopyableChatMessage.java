package mypals.ml.mixin.features.copyableMessages;

import carpet.CarpetServer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, message.getSignedContent()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
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
