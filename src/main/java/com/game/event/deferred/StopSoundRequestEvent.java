package com.game.event.deferred;

import com.game.event.DeferredEvent;
import com.game.sound.SoundRequestId;

public record StopSoundRequestEvent(SoundRequestId requestId) implements DeferredEvent { }