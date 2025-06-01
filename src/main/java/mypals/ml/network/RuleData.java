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

package mypals.ml.network;

import net.minecraft.network.PacketByteBuf;
//#if MC >= 12006
import net.minecraft.network.codec.PacketCodec;
//#endif

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

    //#if MC >= 12006
    public static final PacketCodec<PacketByteBuf, RuleData> CODEC = PacketCodec.of(RuleData::write, RuleData::new);
    //#endif

    public void write(PacketByteBuf buf) {
        buf.writeString(this.name);

        buf.writeString(this.type.toString());

        buf.writeString(this.defaultValue);
        buf.writeString(this.value);
        buf.writeString(this.description);
        AtomicReference<String> suggestions = new AtomicReference<String>();
        suggestions.set("");
        this.suggestions.forEach(
                suggestion -> {
                    if (!Objects.equals(suggestion, "null"))
                        suggestions.set(suggestions + suggestion.toString() + "|");
                }
        );
        buf.writeString(suggestions.toString());

        AtomicReference<String> categories = new AtomicReference<>();
        categories.set("");
        this.categories.forEach(
                category -> {
                    if (!Objects.equals(category, "null"))
                        categories.set(categories + category.toString() + "~");
                }
        );
        buf.writeString(categories.toString());
    }

    public RuleData(PacketByteBuf buf) {
        this(
                buf.readString(), // name
                getRuleType(buf.readString()), // type
                buf.readString(), // defaultValue
                buf.readString(), // value
                buf.readString(), //des
                Arrays.stream(buf.readString().split("\\|")).toList(), //suggestions
                Arrays.stream(buf.readString().split("~")).toList() //categories
        );
    }

    private static Class<?> getRuleType(String name) {
        return switch (name) {
            case "Integer" -> Integer.class;
            case "Boolean" -> Boolean.class;
            case "Float" -> Float.class;
            case "Enum" -> Enum.class;
            default -> String.class;
        };
    }

}
