package mypals.ml.translations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class YetAnotherCarpetAdditionTranslations {
	public static Map<String,String> getTranslations(String lang){
		return getTranslations("assets/yetanothercarpetaddition/lang/%s.json",lang);
	}

	public static Map<String, String> getTranslations(String path,String lang)
	{
		String dataJSON;
		try
		{
			dataJSON = IOUtils.toString(
					Objects.requireNonNull(YetAnotherCarpetAdditionTranslations
							.class.getClassLoader().getResourceAsStream(String.format(path, lang))
					), StandardCharsets.UTF_8);
		}
		 catch (IOException e) {
			e.printStackTrace();
            return null;
        }

        Gson gson = (new GsonBuilder()).enableComplexMapKeySerialization().create();
		return gson.fromJson(dataJSON, (new TypeToken<Map<String, String>>()
		{
		}).getType());
	}
}