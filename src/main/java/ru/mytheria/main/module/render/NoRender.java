package ru.mytheria.main.module.render;

import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BooleanSetting;

/**
 * Модуль для отключения различных визуальных элементов рендеринга.
 */
public class NoRender extends Module {

    // Статический экземпляр, как в вашем Mixin (NoRender.INSTANCE)
    // ВАЖНО: Убедитесь, что ваш механизм загрузки модулей (Module Loader)
    // правильно инициализирует этот синглтон.
    public static NoRender INSTANCE;

    // --- Настройки ---

    // 1. Управление огнем (Используется в вашем Mixin)
    private final BooleanSetting removeFire = new BooleanSetting(
            Text.of("Убрать огонь"),
            Text.of("Убирает наложение огня при горении"),
            () -> true
    ).set(true);

    // 2. Управление тряской камеры
    private final BooleanSetting removeShake = new BooleanSetting(
            Text.of("Убрать тряску"),
            Text.of("Убирает тряску экрана при уроне, взрывах или при прыжке на лошади"),
            () -> true
    ).set(true);

    public NoRender() {
        super(Text.of("NoRender"), Category.RENDER);

        // Инициализация синглтона
        INSTANCE = this;

        // Добавление настроек
        addSettings(removeFire, removeShake);
    }

    // --- Геттеры для Mixins (нужны для доступа к состоянию настроек) ---

    public boolean isRemoveFire() {
        return removeFire.getValue();
    }

    public boolean isRemoveShake() {
        return removeShake.getValue();
    }

}