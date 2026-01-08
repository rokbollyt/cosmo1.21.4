package fun.cosmo.api.events.impl;

import fun.cosmo.api.events.Event;

public class EventRotationFix extends Event {

    private float yaw;
    private float pitch;

    // ОШИБКА: ИСПРАВЛЕНО
    // Добавляем конструктор для установки начальных углов
    public EventRotationFix(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Оставляем конструктор по умолчанию, если он нужен в других местах
    public EventRotationFix() {} // <--- Добавьте это, если он был неявным

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public void cancel() { super.cancel(); }
    public void uncancel() { this.canceled = false; }
}