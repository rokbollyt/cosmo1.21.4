package ru.mytheria.api.util.shader.common.states;

import java.awt.Color;

public record ColorState(int color1, int color2, int color3, int color4) {

	public static final ColorState TRANSPARENT = new ColorState(0, 0, 0, 0);
	public static final ColorState WHITE = new ColorState(-1, -1, -1, -1);

	public ColorState(Color color1, Color color2, Color color3, Color color4) {
		this(color1.getRGB(), color2.getRGB(), color3.getRGB(), color4.getRGB());
	}

    public ColorState(Color color) {
		this(color, color, color, color);
	}
	
	public ColorState(int color) {
		this(color, color, color, color);
	}

}