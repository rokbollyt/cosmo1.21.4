package ru.mytheria.api.client.configuration;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.configuration.impl.ModuleConfiguration;

import java.io.*;
import java.util.List;

public class Configuration implements ConfigurationApi {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void save(String name) {
        File file = new File(mc.runDirectory.getAbsolutePath() + "/mytheria/configs/" + name);

        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        JsonArray mainArray = new JsonArray();
        JsonObject descriptionObject = new JsonObject();

        mainArray.add(descriptionObject);

        Mytheria.getInstance().getModuleManager().forEach(module -> mainArray.add(ModuleConfiguration.asElement(module)));

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(mainArray, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(String name) {
        File file = new File(mc.runDirectory.getAbsolutePath() + "/mytheria/configs/" + name);

        try (Reader reader = new FileReader(file)) {
            ModuleConfiguration.parseJson(gson, reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(String name) {
        File file = new File(mc.runDirectory.getAbsolutePath() + "/mytheria/configs/" + name);

        if (file.exists()) {
            file.deleteOnExit();
        }
    }

    @Override
    public List<String> asList() {
        return List.of();
    }
}
