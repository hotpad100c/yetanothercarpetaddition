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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityArgumentType.class)
public class EntityArgumentMixin {
    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
    private void onListSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (!YetAnotherCarpetAdditionRules.commandEnhance) return;

        CommandSource source = (CommandSource) context.getSource();

        if (!(source instanceof ServerCommandSource serverCommandSource)) return;

        MinecraftServer server = serverCommandSource.getServer();
        List<String> names = new ArrayList<>();

        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof PlayerEntity)) {
                    String name = entity.getDisplayName().getString();
                    names.add(entity.getUuidAsString() + "(%s)".formatted(name) + "(%s)".formatted(world.getRegistryKey().getValue()));
                }
            }
        }

        CommandSource.suggestMatching(names, builder);
        cir.setReturnValue(CompletableFuture.completedFuture(builder.build()));
    }
}
