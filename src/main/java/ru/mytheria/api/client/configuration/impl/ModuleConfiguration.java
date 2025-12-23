package ru.mytheria.api.client.configuration.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.mytheria.Mytheria;
import ru.mytheria.api.module.Module;


import java.io.Reader;

public class ModuleConfiguration {

    public static JsonElement asElement( Module module) {
        JsonObject moduleObject = new JsonObject();

        moduleObject.addProperty("Module-Name", module.getModuleName().getString());
        moduleObject.addProperty("Module-Enabled", module.getEnabled());
        moduleObject.addProperty("Module-Key", module.getKey());
        moduleObject.addProperty("Module-Toggle-Action", module.getAction());
        moduleObject.add("Module-Settings", SettingConfiguration.asElement(module.getSettingLayers()));

        return moduleObject;
    }

    public static void parseJson(Gson gson, Reader reader) {
        JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

        if (!jsonElement.isJsonArray()) return;

        jsonElement.getAsJsonArray().asList().forEach(e -> {
            if (!e.isJsonObject()) return;

            JsonObject jsonObject = e.getAsJsonObject();

            if (!jsonObject.has("Module-Name")) return;

            Module moduleLayer = Mytheria.getInstance().getModuleManager().filter(module -> module.getModuleName().getString().equalsIgnoreCase(jsonObject.get("Module-Name").getAsString())).getFirst();

            moduleLayer.setEnabled(jsonObject.get("Module-Enabled").getAsBoolean());
            moduleLayer.setKey(jsonObject.get("Module-Key").getAsInt());
            moduleLayer.setAction(jsonObject.get("Module-Toggle-Action").getAsInt());

            if (!jsonObject.has("Module-Settings")) return;

            jsonObject.getAsJsonArray("Module-Settings").asList().forEach(settingElement ->
                    SettingConfiguration.parseSetting(moduleLayer, settingElement));
        });
    }
}
