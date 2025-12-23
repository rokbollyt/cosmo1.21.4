package ru.mytheria.api.util.fonts;

import ru.mytheria.api.util.fonts.main.StyledFont;

public class Fonts {
    public static StyledFont[] biko = new StyledFont[41];
    public static StyledFont[] iconf = new StyledFont[41];
    public static StyledFont[] hud = new StyledFont[41];
    public static StyledFont[] ubuntu = new StyledFont[41];
    public static StyledFont[] aaa = new StyledFont[41];
    public static StyledFont[] font = new StyledFont[41];
    public static StyledFont[] gui = new StyledFont[41];


    public static void init() {
        for (int i = 10; i < 41; i++) {
            try {
                System.out.println("Инициализируем biko[" + i + "]");
                biko[i] = new StyledFont("biko", "biko", i);

                System.out.println("Инициализируем iconf[" + i + "]");
                iconf[i] = new StyledFont("iconf", "iconf", i);

                System.out.println("Инициализируем hud[" + i + "]");
                hud[i] = new StyledFont("hud", "hud", i);

                System.out.println("Инициализируем aaa[" + i + "]");
                aaa[i] = new StyledFont("aaa", "aaa", i);

                System.out.println("Инициализируем font[" + i + "]");
                font[i] = new StyledFont("font", "font", i);

                System.out.println("Инициализируем gui[" + i + "]");
                gui[i] = new StyledFont("gui", "gui", i);

            } catch (Exception e) {
                System.err.println("Ошибка при инициализации шрифта на размере " + i);
                e.printStackTrace();
            }
        }
    }
}