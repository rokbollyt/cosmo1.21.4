package ru.mytheria.main.ui.clickGui.components.settings;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.main.ui.clickGui.Component;

@Getter
@RequiredArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class SettingComponent extends Component {

    Setting settingLayer;

}
