package mypals.ml.mixin;

import com.mojang.brigadier.ParseResults;
import mypals.ml.features.visualizingFeatures.*;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
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
            HopperCooldownVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("scheduledTickVisualize") && command.contains("false")) {
            ScheduledTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("randomTickVisualize") && command.contains("false")) {
            RandomTickVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("blockEventVisualize") && command.contains("false")) {
            BlockEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
        if (command.startsWith("carpet") && command.contains("gameEventVisualize") && command.contains("false")) {
            GameEventVisualizing.clearVisualizers(parseResults.getContext().getSource().getServer());
        }
    }
}
