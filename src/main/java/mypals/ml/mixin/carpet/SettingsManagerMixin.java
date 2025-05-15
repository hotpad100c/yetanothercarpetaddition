package mypals.ml.mixin.carpet;

import carpet.api.settings.SettingsManager;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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
