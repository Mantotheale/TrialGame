package com.game.util;

public record Color(float r, float g, float b, float a) {
    public static Color RED = new Color(1, 0, 0, 1);
    public static Color GREEN = new Color(0, 1, 0, 1);
    public static Color BLUE = new Color(0, 0, 1, 1);
}
