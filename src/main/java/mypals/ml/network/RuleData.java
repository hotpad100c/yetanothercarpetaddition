package mypals.ml.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RuleData {
    public String name;
    public String defaultValue;
    public String value;
    public String description;
    public Class<?> type;
    public List<String> suggestions;
    public List<String> categories;

    public RuleData(String name, Class<?> type, String defaultValue, String value, String description, List<String> suggestions, List<String> categories) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
        this.type = type;
        this.suggestions = suggestions;
        this.categories = categories;
    }

    public static final PacketCodec<PacketByteBuf, RuleData> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.name);

                buf.writeString(value.type.toString());
                /*if(value.type == String.class) {
                    buf.writeString("String");
                }
                if(value.type == Integer.class) {
                    buf.writeString("Integer");
                }if(value.type == Boolean.class) {
                    buf.writeString("Boolean");
                }
                if(value.type == Float.class) {
                    buf.writeString("Float");
                }
                if(value.type == Enum.class) {
                    buf.writeString("Enum");
                }*/

                buf.writeString(value.value);
                buf.writeString(value.defaultValue);
                buf.writeString(value.description);
                AtomicReference<String> suggestions = new AtomicReference<String>();
                suggestions.set("");
                value.suggestions.forEach(
                        suggestion -> {
                            if (!Objects.equals(suggestion, "null"))
                                suggestions.set(suggestions + suggestion.toString() + "|");
                        }
                );
                buf.writeString(suggestions.toString());

                AtomicReference<String> categories = new AtomicReference<>();
                categories.set("");
                value.categories.forEach(
                        category -> {
                            if (!Objects.equals(category, "null"))
                                categories.set(categories + category.toString() + "~");
                        }
                );
                buf.writeString(categories.toString());
            },
            buf -> {
                String name = buf.readString();
                Class<?> type = String.class;
                switch (buf.readString()) {
                    case "String" -> type = String.class;
                    case "Integer" -> type = Integer.class;
                    case "Boolean" -> type = Boolean.class;
                    case "Float" -> type = Float.class;
                    case "Enum" -> type = Enum.class;
                }
                String value = buf.readString();
                String defaultValue = buf.readString();
                String des = buf.readString();
                List<String> suggestions = Arrays.stream(buf.readString().split("\\|")).toList();
                List<String> categories = Arrays.stream(buf.readString().split("~")).toList();

                return new RuleData(
                        name,
                        type,
                        defaultValue,
                        value,
                        des,
                        suggestions,
                        categories
                );
            }
    );


}
