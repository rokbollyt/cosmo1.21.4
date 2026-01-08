package fun.cosmo.api.events.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import fun.cosmo.api.events.Event; // Предполагаемый базовый интерфейс/класс

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter @Setter
public class HandAnimationEvent extends Event {

    // --- Поля из nexis.lol.events.item.HandAnimationEvent ---
    private final MatrixStack matrices;
    private final Hand hand;
    private final float swingProgress;

    // --- Поля для отмены (как в nexis.lol.events.api.events.callables.EventCancellable) ---
    private boolean cancelled;

    // Вспомогательные методы для работы с отменой
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void uncancel() {
        this.cancelled = false;
    }
}

// ПРИМЕЧАНИЕ: Вам также понадобится создать HandOffsetEvent и SwingDurationEvent
// аналогичным образом в пакете fun.cosmo.api.events.item, чтобы HandTweaks заработал.