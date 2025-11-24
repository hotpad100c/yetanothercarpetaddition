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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityArgument.class)
public class EntityArgumentMixin {
    @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
    private void onListSuggestions(CommandContext<SharedSuggestionProvider> context, SuggestionsBuilder builder, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        if (YetAnotherCarpetAdditionRules.commandEnhance.equals("false")) return;

        SharedSuggestionProvider source = (SharedSuggestionProvider) context.getSource();

        if (!(source instanceof CommandSourceStack serverCommandSource)) return;

        MinecraftServer server = serverCommandSource.getServer();
        List<String> names = new ArrayList<>();

        for (ServerLevel world : server.getAllLevels()) {
            for (Entity entity : world.getAllEntities()) {
                if (!(entity instanceof Player)) {
                    String name = entity.getDisplayName().getString();
                    names.add(entity.getStringUUID() + "(%s)".formatted(name) + "(%s)".formatted(world.dimension().location()));
                }
            }
        }

        SharedSuggestionProvider.suggest(names, builder);
        cir.setReturnValue(CompletableFuture.completedFuture(builder.build()));
    }
}
