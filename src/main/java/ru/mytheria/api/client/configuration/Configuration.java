package ru.mytheria.api.client.configuration;


import com.google.gson.*;
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

        // ===== FRIENDS SAVE =====
        JsonArray friendsArray = new JsonArray();
        ru.mytheria.api.client.friend.FriendService.getFriends()
                .forEach(friend -> friendsArray.add(friend.getName()));
        descriptionObject.add("Friends", friendsArray);
        // ========================

        mainArray.add(descriptionObject);

        Mytheria.getInstance().getModuleManager()
                .forEach(module -> mainArray.add(ModuleConfiguration.asElement(module)));

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
            JsonElement element = gson.fromJson(reader, JsonElement.class);

            if (!element.isJsonArray()) return;

            JsonArray array = element.getAsJsonArray();
            JsonObject descriptionObject = array.get(0).getAsJsonObject();

            // ===== FRIENDS LOAD =====
            if (descriptionObject.has("Friends")) {
                ru.mytheria.api.client.friend.FriendService.clear();
                descriptionObject.getAsJsonArray("Friends")
                        .forEach(e -> ru.mytheria.api.client.friend.FriendService.addFriend(e.getAsString()));
            }
            // ========================

            ModuleConfiguration.parseJson(gson, new FileReader(file));

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
