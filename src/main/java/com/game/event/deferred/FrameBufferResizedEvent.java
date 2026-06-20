package com.game.event.deferred;

import com.game.event.DeferredEvent;

public record FrameBufferResizedEvent(int newWidth, int newHeight) implements DeferredEvent { }
