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

package mypals.ml.mixin.features.stepCounter;

import com.llamalad7.mixinextras.sugar.Local;
import mypals.ml.features.tickStepCounter.StepManager;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.TickCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @Inject(method = "setFreeze",
            at = @At("RETURN"))
    private static void modifyFeedbackText(CommandSourceStack source, boolean frozen, CallbackInfoReturnable<Integer> cir) {
        StepManager.reset();
    }
    @ModifyArg(
            method = "step(Lnet/minecraft/commands/CommandSourceStack;I)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/CommandSourceStack;sendSuccess(Ljava/util/function/Supplier;Z)V",
                    ordinal = 0
            ),
            index = 0
    )
    private static java.util.function.Supplier<Component> modifyFeedbackText(java.util.function.Supplier<Component> original, @Local(argsOnly = true) int steps) {
        return () -> {
            Component originalText = original.get();
            MutableComponent modifiedText = originalText.copy();
            StepManager.step(steps);
            if(YetAnotherCarpetAdditionRules.enableTickStepCounter) {
                modifiedText.withStyle(style -> style.withHoverEvent(
                        HoverEvent.showText(
                                Component.literal(String.format(Component.translatable("TickStepCounter.stepped").getString(), StepManager.getStepped()))
                        )
                ));
            }
            return modifiedText;
        };
    }

}
