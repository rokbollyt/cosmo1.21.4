package ru.mytheria.api.client.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationService implements ConfigurationApi {

    final Configuration configurationController = new Configuration();

    @Override
    public void save(String name) {
        configurationController.save(name + ".json");
    }

    @Override
    public void load(String name) {
        configurationController.load(name + ".json");
    }

    @Override
    public void remove(String name) {
        configurationController.remove(name);
    }

    @Override
    public List<String> asList() {
        File dir = new File(mc.runDirectory, "mytheria/configs");

        if (!dir.exists() || !dir.isDirectory())
            return List.of();

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null)
            return List.of();

        List<String> result = new ArrayList<>();
        for (File file : files) {
            result.add(file.getName().replace(".json", ""));
        }

        return result;
    }

}
