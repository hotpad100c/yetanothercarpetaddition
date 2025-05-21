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

package mypals.ml.mixin.carpet;

import carpet.api.settings.SettingsManager;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mypals.ml.settings.YetAnotherCarpetAdditionRules;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(SettingsManager.class)
public class SettingsManagerMixin {
    @WrapMethod(
            method = "suggestMatchingContains",remap = false
    )
    private static CompletableFuture<Suggestions> suggestMatchingContains(Stream<String> stream,
                                                                          SuggestionsBuilder suggestionsBuilder,
                                                                          Operation<CompletableFuture<Suggestions>> original) {
        List<String> regularSuggestionList = new ArrayList<>();
        List<String> smartSuggestionList = new ArrayList<>();
        String query = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        stream.forEach((listItem) -> {
            // Regex camelCase Search
            var words = Arrays.stream(listItem.split("(?<!^)(?=[A-Z])")).map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
            var prefixes = new ArrayList<String>(words.size());
            for (int i = 0; i < words.size(); i++)
                prefixes.add(String.join("", words.subList(i, words.size())));
            if (prefixes.stream().anyMatch(s -> s.startsWith(query))) {
                smartSuggestionList.add(listItem);
            }
            // Regular prefix matching, reference: CommandSource.suggestMatching
            if (CommandSource.shouldSuggest(query, listItem.toLowerCase(Locale.ROOT))) {
                regularSuggestionList.add(listItem);
            }
        });
        var filteredSuggestionList = getFullSyggestionList(regularSuggestionList, smartSuggestionList);
        Objects.requireNonNull(suggestionsBuilder);
        filteredSuggestionList.forEach(suggestionsBuilder::suggest);
        return suggestionsBuilder.buildFuture();

    }
    @Unique
    private static List<String> getFullSyggestionList(List<String> regular, List<String> smart) {
        if(YetAnotherCarpetAdditionRules.mergeSmartAndRegularCommandSuggestions) {
            return Stream.concat(regular.stream(), smart.stream()).distinct().collect(Collectors.toList());
        }else{
            return regular.isEmpty() ? smart : regular;
        }
    }
}
