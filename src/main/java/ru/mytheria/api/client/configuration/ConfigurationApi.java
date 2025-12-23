package ru.mytheria.api.client.configuration;

import ru.mytheria.api.clientannotation.QuickApi;

import java.util.List;

public interface ConfigurationApi extends QuickApi {

    void save(String name);

    void load(String name);

    void remove(String name);

    List<String> asList();

}
