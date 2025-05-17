package mypals.ml.mixin.features.stepCounter;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TickCommand;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @Inject(method = "executeFreeze",
            at = @At("RETURN"))
    private static void modifyFeedbackText(ServerCommandSource source, boolean frozen, CallbackInfoReturnable<Integer> cir) {
        StepManager.reset();
    }
    @ModifyArg(
            method = "executeStep(Lnet/minecraft/server/command/ServerCommandSource;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Ljava/util/function/Supplier;Z)V",
                    ordinal = 0
            ),
            index = 0
    )
    private static java.util.function.Supplier<Text> modifyFeedbackText(java.util.function.Supplier<Text> original, @Local(argsOnly = true) int steps) {
        return () -> {
            Text originalText = original.get();
            MutableText modifiedText = originalText.copy();
            StepManager.step(steps);
            if(YetAnotherCarpetAdditionRules.enableTickStepCounter) {
                modifiedText.styled(style -> style.withHoverEvent(
                        new HoverEvent.ShowText(Text.literal(String.format(Text.translatable("TickStepCounter.stepped").getString(), StepManager.getStepped())))
                ));
            }
            return modifiedText;
        };
    }
}
