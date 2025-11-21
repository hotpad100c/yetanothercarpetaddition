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

package mypals.ml.translations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class YetAnotherCarpetAdditionTranslations {
    public static Map<String, String> getTranslations(String lang) {
        return getTranslations("assets/yetanothercarpetaddition/lang/%s.json", lang);
    }

    public static Map<String, String> getTranslations(String path, String lang) {
        Map<String, String> translations = new HashMap<>();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

        String dataJSON = loadJson(String.format(path, lang));
        if (dataJSON != null) {
            Map<String, String> parsed = gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
            }.getType());
            if (parsed != null) {
                translations.putAll(parsed);
            }
        }

        if (!lang.equals("en_us") && translations.isEmpty()) {
            dataJSON = loadJson(String.format(path, "en_us"));
            if (dataJSON != null) {
                Map<String, String> parsed = gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
                }.getType());
                if (parsed != null) {
                    translations.putAll(parsed);
                }
            }
        }

        return translations;
    }

    private static String loadJson(String filePath) {
        try {
            InputStream inputStream = YetAnotherCarpetAdditionTranslations.class.getClassLoader()
                    .getResourceAsStream(filePath);
            if (inputStream == null) {
                System.err.println("Language file not found: " + filePath);
                return null;
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}