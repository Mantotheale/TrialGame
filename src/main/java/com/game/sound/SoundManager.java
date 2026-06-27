package com.game.sound;

import com.game.event.bus.EventBus;
import com.game.event.deferred.PlaySoundRequestEvent;
import com.game.event.deferred.StopSoundRequestEvent;
import com.game.resourcemanager.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {
    private final static int MAX_CONCURRENT_SOUNDS = 8;

    private final ResourceManager resourceManager;
    private final List<SoundSource> sources;
    private final List<SoundRequestId> playingSounds;

    public SoundManager(EventBus bus, ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.sources = new ArrayList<>();
        this.playingSounds = new ArrayList<>();

        for (int i = 0; i < MAX_CONCURRENT_SOUNDS; i++) {
            sources.add(new SoundSource());
            playingSounds.add(null);
        }

        bus.addObserver(PlaySoundRequestEvent.class, this::onPlaySoundRequest);
        bus.addObserver(StopSoundRequestEvent.class, this::onStopSoundRequest);
    }

    public void delete() {
        for (SoundSource s: sources)
            s.delete();
    }

    private void onPlaySoundRequest(EventBus bus, PlaySoundRequestEvent event) {
        SoundBuffer buffer = resourceManager.getSoundBuffer(event.sound());

        for (int i = 0; i < sources.size(); i++) {
            SoundSource source = sources.get(i);

            if (!source.isPlaying()) {
                source.play(buffer, event.doesLoop(), event.gain());
                playingSounds.set(i, event.requestId());
                break;
            }
        }
    }

    private void onStopSoundRequest(EventBus bus, StopSoundRequestEvent event) {
        for (int i = 0; i < playingSounds.size(); i++) {
            SoundRequestId requestId = playingSounds.get(i);

            if (event.requestId().equals(requestId))
                sources.get(i).stop();
        }
    }
}
