package mypals.ml.translations;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import mypals.ml.YetAnotherCarpetAdditionServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class YACALanguageUtil {
    private static final Gson GSON = new Gson();
    private static final Map<String, Map<String, String>> TRANSLATION_CACHE = new HashMap<>();

    public static String getTranslation(String lang, String key) {
        Map<String, String> translations = getTranslations(lang);
        String translation = translations.get(key);
        if (translation != null) {
            return translation;
        }
        if (!lang.equals("en_us")) {
            translations = getTranslations("en_us");
            translation = translations.get(key);
            if (translation != null) {
                return translation;
            }
        }

        return key.replace("carpet.rule.", "")
                .replace(".name", "")
                .replace(".desc", "");
    }


    private static Map<String, String> getTranslations(String lang) {
        String cacheKey = lang;
        if (TRANSLATION_CACHE.containsKey(cacheKey)) {
            return TRANSLATION_CACHE.get(cacheKey);
        }

        Map<String, String> translations = new HashMap<>();

        String carpetPath = String.format("assets/carpet/lang/%s.json", lang);
        loadTranslationsFromPath("carpet", carpetPath, translations);

        for (CarpetExtension ext : CarpetServer.extensions) {
            Map<String, String> extMappings = ext.canHasTranslations(lang);
            if (extMappings == null) {
                continue;
            }

            boolean warned = false;
            for (Map.Entry<String, String> entry : extMappings.entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith("carpet.")) {
                    if (key.startsWith("rule.")) {
                        key = "carpet.rule." + key.substring(5);
                    } else if (key.startsWith("category.")) {
                        key = "carpet.category." + key.substring(9);
                    }
                    if (!warned && !key.equals(entry.getKey())) {
                        YetAnotherCarpetAdditionServer.LOGGER.warn("Found outdated translation keys in extension '{}'! " +
                                "These won't be supported in a later Carpet version! " +
                                "Mapped key: {} -> {}", ext.getClass().getName(), entry.getKey(), key);
                        warned = true;
                    }
                }
                translations.putIfAbsent(key, entry.getValue());
            }
        }
        translations.keySet().removeIf(key -> {
            if (key.startsWith("//")) {
                YetAnotherCarpetAdditionServer.LOGGER.warn("Found translation key starting with //: '{}'. " +
                        "This is deprecated, consider using GSON lenient mode with regular comments.", key);
                return true;
            }
            return false;
        });

        TRANSLATION_CACHE.put(cacheKey, translations);
        return translations;
    }

    private static void loadTranslationsFromPath(String namespace, String path, Map<String, String> translations) {
        try {
            // 尝试从模组资源加载
            Path resourcePath = FabricLoader.getInstance()
                    .getModContainer(namespace)
                    .orElse(null)
                    .findPath(path)
                    .orElse(null);

            if (resourcePath != null && Files.exists(resourcePath)) {
                String jsonContent = Files.readString(resourcePath, StandardCharsets.UTF_8);
                JsonObject jsonObject = GSON.fromJson(jsonContent, JsonObject.class);
                jsonObject.entrySet().forEach(entry -> {
                    if (!entry.getKey().startsWith("//")) {
                        translations.putIfAbsent(entry.getKey(), JsonHelper.getString(jsonObject, entry.getKey(), entry.getKey()));
                    }
                });
            } else {
                try (InputStream inputStream = YACALanguageUtil.class.getClassLoader().getResourceAsStream(path)) {
                    if (inputStream != null) {
                        String jsonContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        JsonObject jsonObject = GSON.fromJson(jsonContent, JsonObject.class);
                        jsonObject.entrySet().forEach(entry -> {
                            if (!entry.getKey().startsWith("//")) {
                                translations.putIfAbsent(entry.getKey(), JsonHelper.getString(jsonObject, entry.getKey(), entry.getKey()));
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            YetAnotherCarpetAdditionServer.LOGGER.warn("Failed to load language file: {} for namespace: {}, error: {}", path, namespace, e.getMessage());
        }
    }

    public static void clearCache() {
        TRANSLATION_CACHE.clear();
    }
}
