package ru.mytheria.api.client;

import java.util.Date;

public class Profile {
    private final String role;
    private final String name;

    public Profile(Role role, String name) {
        this.role = String.valueOf(role);
        this.name = name;
    }

    public static Profile create(Date issueDat) {
        return new Profile(
                Role.DEVELOEPR,
                "miuroz"
        );
    }

    public enum Role {
        DEVELOEPR,
        TEST,
        USER
    }
}
