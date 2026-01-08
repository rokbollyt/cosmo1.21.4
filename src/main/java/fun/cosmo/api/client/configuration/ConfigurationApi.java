package fun.cosmo.api.client.configuration;

import fun.cosmo.api.clientannotation.QuickApi;

import java.util.List;

public interface ConfigurationApi extends QuickApi {

    void save(String name);

    void load(String name);

    void remove(String name);

    List<String> asList();

}
