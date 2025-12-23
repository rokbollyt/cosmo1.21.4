package ru.mytheria.api.client.localization;

import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import net.minecraft.util.Identifier;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickImport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Localization implements QuickImport {
    private static final Map<Language, Map<String, String>> cache = new ConcurrentHashMap<>();
    private static boolean translationsLoaded = false;

    public static String get(String key) {
        return get(key, (Object[]) null);
    }

    public static String get(String key, Object... args) {
        if (!translationsLoaded) {
            if (mc.getResourceManager() != null) {
                loadAllTranslations();
                translationsLoaded = true;
            } else {
                System.err.println("[Localization] ResourceManager не инициализирован, пропускаем загрузку переводов");
                return key;
            }
        }

        Language currentLanguage = Mytheria.getInstance().getLanguage();
        Map<String, String> translations = cache.get(currentLanguage);

        if (translations == null) {
            if (mc.getResourceManager() != null) {
                translations = loadTranslations(currentLanguage);
                cache.put(currentLanguage, translations);
            } else {
                System.err.println("[Localization] ResourceManager не инициализирован при попытке загрузить язык: " + currentLanguage.getFile());
                return key;
            }
        }

        String format = translations.getOrDefault(key, key);

        if (args == null || args.length == 0) {
            return format;
        }

        try {
            return String.format(format, args);
        } catch (Exception e) {
            System.err.println("Ошибка форматирования локализации для ключа: " + key + ", формат: " + format);
            e.printStackTrace();
            return format;
        }
    }

    @SneakyThrows
    private static Map<String, String> loadTranslations(Language language) {
        Identifier identifier = Identifier.of("mytheria", "localization/" + language.getFile() + ".json");

        InputStream stream = mc.getResourceManager()
                .getResource(identifier)
                .get()
                .getInputStream();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        );

        return gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
    }

    public static void loadAllTranslations() {
        if (mc.getResourceManager() == null) {
            System.err.println("[Localization] ResourceManager не инициализирован — загрузка переводов пропущена");
            return;
        }

        System.out.println("[Localization] Начинаем загрузку всех переводов...");
        for (Language lang : Language.values()) {
            System.out.println("[Localization] Загружаем язык: " + lang.getFile());
            Map<String, String> translations = loadTranslations(lang);
            cache.put(lang, translations);
        }
        translationsLoaded = true;
        System.out.println("[Localization] Все переводы загружены.");
    }


}