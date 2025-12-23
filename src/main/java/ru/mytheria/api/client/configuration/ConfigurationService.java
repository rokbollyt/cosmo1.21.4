package ru.mytheria.api.client.configuration;

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
        return configurationController.asList();
    }
}
