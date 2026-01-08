package fun.cosmo.main.ui.clickGui;

import fun.cosmo.Mytheria;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.*;
import fun.cosmo.api.module.settings.impl.*;
import fun.cosmo.main.ui.clickGui.components.module.ModuleComponent;
import fun.cosmo.main.ui.clickGui.components.settings.SettingComponent;
import fun.cosmo.main.ui.clickGui.components.settings.bindSetting.BindSettingComponent;
import fun.cosmo.main.ui.clickGui.components.settings.booleanSetting.BooleanSettingComponent;
import fun.cosmo.main.ui.clickGui.components.settings.collection.CollectionComponent;
import fun.cosmo.main.ui.clickGui.components.settings.modeListSetting.ModeListSettingComponent;
import fun.cosmo.main.ui.clickGui.components.settings.modeSetting.ModeSettingComponent;
import fun.cosmo.main.ui.clickGui.components.settings.sliderSetting.SliderSettingComponent;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class Helper {

    public static List<ModuleComponent> moduleLayers( Category category, Predicate<Module> predicate) {
        return Mytheria.getInstance().getModuleManager().getModuleLayers().stream()
                .filter(e -> e.getCategory().equals(category))
                .filter(predicate)
                .map(ModuleComponent::new)
                .toList();
    }

    public static List<SettingComponent> settingComponents( Module moduleLayer) {
        return moduleLayer.getSettingLayers().stream()
                .map(Helper::find)
                .filter(Objects::nonNull)
                .toList();
    }

    public static float moduleHeight(List<SettingComponent> settingComponents) {
        return 40f / 2 + settingComponents.stream()
                                .filter(e -> e.getSettingLayer().getVisible().get())
                                .map(e -> e.getHeight() + 5)
                                .reduce(0f, Float::sum);
    }

    public static SettingComponent find( Setting settingLayer) {
        if (settingLayer instanceof BooleanSetting) return new BooleanSettingComponent(settingLayer);
        if (settingLayer instanceof Collection collection) return new CollectionComponent(collection);
        if (settingLayer instanceof SliderSetting sliderSetting) return new SliderSettingComponent(sliderSetting);
        if (settingLayer instanceof BindSetting bindSetting) return new BindSettingComponent(bindSetting);
        if (settingLayer instanceof ModeSetting modeSetting) return new ModeSettingComponent(modeSetting);
        if (settingLayer instanceof ModeListSetting modeListSetting) return new ModeListSettingComponent(modeListSetting);

        return null;
    }

}
