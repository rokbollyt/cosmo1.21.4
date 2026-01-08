package fun.cosmo.api.client.configuration.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.*;
import fun.cosmo.api.module.settings.impl.*;


import java.util.List;
import java.util.Objects;

public class SettingConfiguration {

    // ... Метод asElement остается без изменений ...
    public static JsonElement asElement(List<Setting> settings) {
        JsonArray settingsArray = new JsonArray();

        settings.forEach(e -> {
            JsonObject settingObject = new JsonObject();

            settingObject.addProperty("Setting-Name", e.getName().getString());

            if (e instanceof Collection collection) {
                settingObject.add("Collection-Settings", asElement(collection.getSettingLayers()));
            }

            if (e instanceof BooleanSetting booleanSetting) {
                settingObject.addProperty("Boolean-Enabled", booleanSetting.getEnabled());
            }

            else if (e instanceof SliderSetting sliderSetting) {
                settingObject.addProperty("Slider-Value", sliderSetting.getValue());
            }

            else if (e instanceof ModeSetting modeSetting) {
                settingObject.addProperty("Mode-Setting-Value", modeSetting.getValue() == null ? "N/A" : modeSetting.getValue());
            }

            else if (e instanceof ModeListSetting modeListSetting) {
                JsonArray valuesArray = new JsonArray();

                modeListSetting.getSelected().forEach(valuesArray::add);

                settingObject.add("Mode-List-Setting-Selected", valuesArray);
            }

            else if (e instanceof BindSetting bindSetting) {
                settingObject.addProperty("Bind-Setting-Selected", bindSetting.getSelected());
                settingObject.addProperty("Bind-Setting-Value", bindSetting.getKey());
            }

            settingsArray.add(settingObject);
        });

        return settingsArray;
    }

    public static void parseSetting( Module moduleLayer, JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();

        Setting settingLayer = moduleLayer.getSettingLayers().stream()
                .filter(e -> e.getName().getString().equalsIgnoreCase(jsonObject.get("Setting-Name").getAsString()))
                .findFirst()
                .orElse(null);

        if (Objects.isNull(settingLayer)) return;

        switch (settingLayer) {
            case BooleanSetting booleanSetting -> {
                JsonElement enabledElement = jsonObject.get("Boolean-Enabled");
                if (enabledElement != null && enabledElement.isJsonPrimitive()) {
                    booleanSetting.set(enabledElement.getAsBoolean());
                }
            }
            case SliderSetting sliderSetting -> {
                JsonElement valueElement = jsonObject.get("Slider-Value");
                if (valueElement != null && valueElement.isJsonPrimitive()) {
                    sliderSetting.set(valueElement.getAsFloat());
                }
            }
            case BindSetting bindSetting -> {
                JsonElement selectedElement = jsonObject.get("Bind-Setting-Selected");
                JsonElement valueElement = jsonObject.get("Bind-Setting-Value");

                if (selectedElement != null && selectedElement.isJsonPrimitive()) {
                    bindSetting.setSelected(selectedElement.getAsBoolean());
                }
                if (valueElement != null && valueElement.isJsonPrimitive()) {
                    bindSetting.set(valueElement.getAsInt());
                }
            }
            case ModeSetting modeSetting -> {
                JsonElement valueElement = jsonObject.get("Mode-Setting-Value");
                if (valueElement != null && valueElement.isJsonPrimitive()) {
                    String value = valueElement.getAsString();
                    modeSetting.set(Objects.equals(value, "N/A") ? null : value);
                }
            }
            case ModeListSetting modeListSetting -> {
                JsonElement listElement = jsonObject.get("Mode-List-Setting-Selected");
                if (listElement != null && listElement.isJsonArray()) {
                    JsonArray jsonArray = listElement.getAsJsonArray();

                    modeListSetting.getValues().forEach(e -> e.set(false));
                    jsonArray.asList().forEach(e -> {
                        if (e.isJsonPrimitive()) { // Дополнительная проверка, что элемент массива - примитив
                            modeListSetting.get(e.getAsString()).set(true);
                        }
                    });
                }
            }
            default -> {
            }
        }
    }
}