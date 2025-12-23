package ru.mytheria.api.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum Category {

    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
