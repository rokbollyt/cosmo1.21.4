package fun.cosmo.main.ui.clickGui.components.settings.modeSetting;

import fun.cosmo.api.module.settings.impl.ModeSetting;

import java.util.List;

public final class ModeSettingHelper {

    public static List<ModeSettingValueComponent> values( ModeSetting modeSetting) {
        return modeSetting.getValues().stream()
                .map(e -> new ModeSettingValueComponent(modeSetting, e))
                .toList();
    }

}