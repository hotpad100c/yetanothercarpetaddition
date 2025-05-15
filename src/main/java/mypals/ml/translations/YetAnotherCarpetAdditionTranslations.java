package mypals.ml.translations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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