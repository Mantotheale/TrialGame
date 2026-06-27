package com.game.event.deferred;

import com.game.event.DeferredEvent;
import com.game.sound.SoundRequestId;
import com.game.sound.Sound;

public record PlaySoundRequestEvent(
        SoundRequestId requestId,
        Sound sound,
        boolean doesLoop,
        float gain
) implements DeferredEvent {
    public static PlaySoundRequestEvent generateEvent(Sound sound, boolean doesLoop, float gain) {
        return new PlaySoundRequestEvent(SoundRequestId.generateId(), sound, doesLoop, gain);
    }
}
