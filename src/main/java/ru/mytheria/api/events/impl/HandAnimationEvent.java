package ru.mytheria.api.events.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import ru.mytheria.api.events.callables.EventCancellable; // Используем ваш пакет

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter @Setter
public class HandAnimationEvent extends EventCancellable {
    private final MatrixStack matrices;
    private final Hand hand;
    private final float swingProgress;
}
