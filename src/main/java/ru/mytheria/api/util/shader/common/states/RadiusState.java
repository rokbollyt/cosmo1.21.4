package ru.mytheria.api.util.shader.common.states;

public record RadiusState(float radius1, float radius2, float radius3, float radius4) {

	public static final RadiusState NO_ROUND = new RadiusState(0.0f, 0.0f, 0.0f, 0.0f);

	public RadiusState(double radius1, double radius2, double radius3, double radius4) {
		this((float) radius1, (float) radius2, (float) radius3, (float) radius4);
	}

	public RadiusState(double radius) {
		this(radius, radius, radius, radius);
	}

	public RadiusState(float radius) {
		this(radius, radius, radius, radius);
	}

}