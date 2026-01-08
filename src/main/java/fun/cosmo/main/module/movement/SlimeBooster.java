package fun.cosmo.main.module.movement; // Или другой подходящий пакет

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import fun.cosmo.Mytheria;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.impl.SliderSetting;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlimeBooster extends Module {

    private static final SliderSetting boostHeightSetting = new SliderSetting(
            Text.of("Высота прыжка"),
            Text.of("Множитель скорости при отталкивании от слизняка"),
            () -> true
    ).set(0.5F, 5.0F, 0.1F).set(1.5F);

    public static SlimeBooster getInstance() {
        return (SlimeBooster) Mytheria.getInstance().getModuleManager().find(SlimeBooster.class);
    }

    public float getBoostHeight() {
        return boostHeightSetting.getValue();
    }

    public SlimeBooster() {
        super(Text.of("Slime Booster"), Text.of("Увеличивает высоту прыжка со слайм-блоков"), Category.MOVEMENT);
        addSettings(boostHeightSetting);
    }
}