package mypals.ml.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class RuleData<T> {
    public String name;
    public T defaultValue;
    public T value;
    public String description;
    public Class<T> type;
    List<String> suggestions;
    List<String> categories;
    public RuleData(String name, T defaultValue, T value, String description,List<String> suggestions, List<String> categories,  Class<T> type) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
        this.type = type;
        this.suggestions = suggestions;
        this.categories = categories;
    }
    public static <T> Codec<RuleData<T>> createRuleDataCodec(Codec<T> valueCodec, Class<T> typeClass) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(r -> r.name),
                valueCodec.fieldOf("defaultValue").forGetter(r -> r.defaultValue),
                valueCodec.fieldOf("value").forGetter(r -> r.value),
                Codec.STRING.fieldOf("description").forGetter(r -> r.description),
                Codec.STRING.listOf().fieldOf("suggestions").forGetter(r -> r.suggestions),
                Codec.STRING.listOf().fieldOf("categories").forGetter(r -> r.categories),
                Codec.unit(typeClass).fieldOf("type").forGetter(r -> r.type)
        ).apply(instance, RuleData::new
        ));
    }


}
