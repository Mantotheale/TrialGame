package com.game.event;

public record FrameBufferResizedEvent(int newWidth, int newHeight) implements Event { }
