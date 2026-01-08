package fun.cosmo.main.module.movement;

import net.minecraft.text.Text;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.impl.SliderSetting;

public class ElytraForward extends Module {

    public final SliderSetting forward = new SliderSetting(Text.of("Форвард"), null, () -> true).set(3f, 6f, 1f);

    public ElytraForward() {
        super(Text.of("ElytraForward"), Category.MOVEMENT);
    }
}