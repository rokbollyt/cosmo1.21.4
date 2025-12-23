package ru.mytheria.api.util.fonts.main;

import ru.mytheria.api.clientannotation.QuickApi;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class MsdfUtil {

    public static String cutString(String string, int size, float width) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder();
        String[] split = string.split(" ");

        AtomicReference<Float> lineWidth = new AtomicReference<>(0f);
        Arrays.stream(split).forEach(e -> {
            float textWidth = QuickApi.inter().getWidth(e, size);

            if (lineWidth.get() + textWidth > width) {
                stringBuilder.append("\n");
                lineWidth.set(0f);
            } else if (lineWidth.get() > 0) {
                stringBuilder.append(" ");
                lineWidth.set(lineWidth.get() + QuickApi.inter().getWidth(" ", size));
            }

            lineWidth.set(lineWidth.get() + textWidth);
            stringBuilder.append(e);
        });

        return stringBuilder.toString();
    }

}
