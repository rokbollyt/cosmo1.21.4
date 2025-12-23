package ru.mytheria.api.client.localization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@RequiredArgsConstructor
public enum Language {
    ENG("en_us"),
    RUS("ru_ru");

    private final String file;
    private final HashMap<String, String> strings = new HashMap<>();

    @Setter
    private static Language current = ENG;

    public static Language getCurrent() {
        return current;
    }
}
