package ru.mytheria.api.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.mytheria.api.client.configuration.ConfigurationService;
import ru.mytheria.api.client.friend.FriendService;

public final class ChatCommandExecutor {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ConfigurationService CONFIG = new ConfigurationService();

    public static void execute(String raw) {
        if (mc.player == null) return;

        String[] args = raw.trim().split(" ");
        if (args.length == 0) return;

        switch (args[0].toLowerCase()) {

            case "cfg" -> handleCfg(args);
            case "friend" -> handleFriend(args);
        }
    }

    private static void handleCfg(String[] args) {
        if (args.length < 2) {
            msg(".cfg save/load/delete/list <name>");
            return;
        }

        switch (args[1]) {
            case "save" -> {
                CONFIG.save(args[2]);
                msg("Config saved: " + args[2]);
            }
            case "load" -> {
                CONFIG.load(args[2]);
                msg("Config loaded: " + args[2]);
            }
            case "delete" -> {
                CONFIG.remove(args[2]);
                msg("Config deleted: " + args[2]);
            }
            case "list" -> msg("Configs: " + CONFIG.asList());
        }
    }

    private static void handleFriend(String[] args) {
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

    private static void msg(String text) {
        mc.player.sendMessage(Text.literal(text), false);
    }
}
