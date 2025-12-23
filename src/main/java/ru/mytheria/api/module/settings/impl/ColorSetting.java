package ru.mytheria.api.module.settings.impl;

import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;

import java.awt.*;
import java.util.function.Supplier;

import static net.minecraft.util.math.ColorHelper.*;

public class ColorSetting extends Setting {

    float hsb, saturation, brightness = 0.0f, alpha = 1.0f;

    public ColorSetting(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);
    }

    public ColorSetting set(Integer color) {
        float[] hsbValue = Color.RGBtoHSB(getRed(color), getGreen(color), getBlue(color), new float[3]);

        this.hsb = hsbValue[0];
        this.saturation = hsbValue[1];
        this.brightness = hsbValue[2];
        this.alpha = getAlpha(color);

        return this;
    }

    public ColorSetting set(float hsb, float saturation, float brightness, float alpha) {
        this.hsb = hsb;
        this.saturation = saturation;
        this.brightness = brightness;
        this.alpha = alpha;

        return this;
    }


    @Override
    public ColorSetting collection(Collection collection) {
        collection.put(this);

        return this;
    }
}
