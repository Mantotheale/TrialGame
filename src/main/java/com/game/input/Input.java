package com.game.input;

public sealed interface Input permits KeyInput, ResizeFrameBuffer, CloseWindow { }