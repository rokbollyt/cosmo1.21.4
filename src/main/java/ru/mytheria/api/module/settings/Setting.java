package ru.mytheria.api.module.settings;

/*
 * Create by puzatiy
 * At 03.06.2025
 */


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Setting implements SettingApi {

    Text name;
    Text description;
    Supplier<Boolean> visible;
    Animation animation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    public Text getDescription() {
        return description == null ? Text.of("") : description;
    }

   /* public void reg( Module provider) {
        provider.getSettingLayers().add(this);
    }*/
}
