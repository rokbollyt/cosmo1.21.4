package ru.mytheria.api.events.impl;



import ru.mytheria.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.math.Vec3d;

public class CollisionEvent extends Event {

    @Getter
    @AllArgsConstructor
    public static class PlayerCollisionEvent extends CollisionEvent { }

    @AllArgsConstructor
    public static class BlocksCollisionEvent extends CollisionEvent {
        Vec3d motion;
    }

}