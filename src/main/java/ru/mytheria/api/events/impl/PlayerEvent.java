package ru.mytheria.api.events.impl;


import ru.mytheria.api.events.Event;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.Vec3d;

public class PlayerEvent extends Event {

    public static class DeathEvent extends PlayerEvent { }

    public static class MovementEvent extends PlayerEvent { }

    @Setter
    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VelocityEvent extends PlayerEvent {
        Vec3d input;
        float speed;
        float yaw;
        Vec3d velocity;
    }
}
