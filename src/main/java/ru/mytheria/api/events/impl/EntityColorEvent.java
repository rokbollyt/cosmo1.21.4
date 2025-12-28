package ru.mytheria.api.events.impl;

import net.minecraft.entity.Entity;
import meteordevelopment.orbit.ICancellable;

// В вашем примере нет Entity, но для правильной работы KillAura он нужен
// Добавляем Entity для корректной работы с невидимостью
public class EntityColorEvent implements ICancellable {
    private boolean cancelled = false;
    private int color;
    private final Entity entity; // Добавляем Entity

    public EntityColorEvent(int color, Entity entity) {
        this.color = color;
        this.entity = entity;
    }

    // Геттеры и Сеттеры
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public Entity getEntity() { return entity; }

    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}