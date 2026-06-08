package com.game.input;

public record CloseWindow() implements Input {
    public static CloseWindow INSTANCE = new CloseWindow();
}
