package mypals.ml.mixin.features.betterCommmand;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(KillCommand.class)
public class KillCommandMixin {
    @Inject(
            method = "execute",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onExecute(ServerCommandSource source, Collection<? extends Entity> targets, CallbackInfoReturnable<Integer> cir) {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;
        Map<String, Integer> typeCounts = new HashMap<>();

        for (Entity entity : targets) {
            entity.kill((ServerWorld) entity.getWorld());

            String typeName = entity.getDisplayName().getString();
            typeCounts.merge(typeName, 1, Integer::sum);
        }

        int total = targets.size();
        MutableText baseMessage;
        if (total == 1) {
            baseMessage = Text.translatable("commands.kill.success.single", targets.iterator().next().getDisplayName());
        } else {
            baseMessage = Text.translatable("commands.kill.success.multiple", total);
        }

        StringBuilder tooltipBuilder = new StringBuilder("");
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            tooltipBuilder.append("• %s x%d\n".formatted(entry.getKey(), entry.getValue()));
        }

        baseMessage.styled(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(tooltipBuilder.toString()))
        ));

        source.sendFeedback(() -> baseMessage, true);
        cir.setReturnValue(total);
    }
}
