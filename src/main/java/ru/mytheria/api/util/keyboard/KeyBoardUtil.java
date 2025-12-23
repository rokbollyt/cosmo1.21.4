package ru.mytheria.api.util.keyboard;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import ru.mytheria.api.clientannotation.QuickImport;

import java.util.Locale;

import static net.minecraft.client.util.InputUtil.fromTranslationKey;

public class KeyBoardUtil implements QuickImport {

    public static boolean isKeyPressed(KeyBinding key) {
        if (key.getDefaultKey().getCode() == -1) return false;

        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), fromTranslationKey(key.getBoundKeyTranslationKey()).getCode());
    }

    public static String translate(int keyCodeIn) {
        InputUtil.Key key = keyCodeIn < 8 ?
                InputUtil.Type.MOUSE.createFromCode(keyCodeIn) :
                InputUtil.Type.KEYSYM.createFromCode(keyCodeIn);

        return keyCodeIn == -1 ? "N/A" : key.getTranslationKey()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "MOUSE ")
                .toUpperCase(Locale.ROOT);
    }

}
