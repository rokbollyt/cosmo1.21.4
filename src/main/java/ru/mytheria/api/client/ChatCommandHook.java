package ru.mytheria.api.client;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.mytheria.api.client.configuration.ConfigurationService;
import ru.mytheria.api.client.friend.FriendService;

public final class ChatCommandHook {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ConfigurationService CONFIG = new ConfigurationService();

    public static void init() {

        ClientSendMessageEvents.CHAT.register(message -> {

            if (!message.startsWith(".")) return;

            handle(message.substring(1).trim());
        });
    }

    private static void handle(String input) {
        if (input.isEmpty() || mc.player == null) return;

        String[] args = input.split(" ");

        switch (args[0].toLowerCase()) {

            case "friend" -> {
                if (args.length < 3) {
                    msg(".friend add/remove <name>");
                    return;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    FriendService.addFriend(args[2]);
                    msg("Friend added: " + args[2]);
                }

                if (args[1].equalsIgnoreCase("remove")) {
                    FriendService.removeFriend(args[2]);
                    msg("Friend removed: " + args[2]);
                }
            }

            case "cfg" -> {
                if (args.length < 2) {
                    msg(".cfg save/load/remove/list");
                    return;
                }

                switch (args[1]) {
                    case "save" -> CONFIG.save(args.length > 2 ? args[2] : "default");
                    case "load" -> CONFIG.load(args.length > 2 ? args[2] : "default");
                    case "remove" -> CONFIG.remove(args[2]);
                    case "list" -> msg("Configs: " + CONFIG.asList());
                }
            }
        }
    }

    private static void msg(String text) {
        mc.player.sendMessage(Text.literal(text), false);
    }
}
