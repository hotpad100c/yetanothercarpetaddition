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

package mypals.ml.mixin.features.betterCommmand;

import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import mypals.ml.utils.adapter.HoverEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#if MC >= 12102
//$$ import net.minecraft.server.world.ServerWorld;
//#endif

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
            entity.kill(
                    //#if MC >= 12102
                    //$$ (ServerWorld) entity.getWorld()
                    //#endif
            );

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
                HoverEvent.showText(Text.literal(tooltipBuilder.toString()))
        ));

        source.sendFeedback(() -> baseMessage, true);
        cir.setReturnValue(total);
    }
}
