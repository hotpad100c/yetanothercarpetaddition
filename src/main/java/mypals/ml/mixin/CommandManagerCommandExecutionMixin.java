package mypals.ml.mixin;

import com.mojang.brigadier.ParseResults;
import mypals.ml.YetAnotherCarpetAdditionServer;
import mypals.ml.features.visualizingFeatures.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerCommandExecutionMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private void onCommandExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
        if (command.startsWith("carpet") && command.contains("hopperCooldownVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.hopperCooldownVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("scheduledTickVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.scheduledTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("randomTickVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.randomTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("blockEventVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("gameEventVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.gameEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("blockUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.NC);
        }
        if (command.startsWith("carpet") && command.contains("stateUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.PP);
        }
        if (command.startsWith("carpet") && command.contains("comparatorUpdateVisualize") && command.contains("false")) {
            YetAnotherCarpetAdditionServer.blockUpdateVisualizing.clearVisualizers(parseResults.getContext().getSource(), BlockUpdateVisualizing.UpdateType.CP);
        }
    }
}
