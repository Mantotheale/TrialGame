package com.game.sound;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

public class SoundSource {
    private final int sourceId;
    private boolean isPlaying;

    public SoundSource() {
        sourceId = alGenSources();
        isPlaying = false;
    }

    public void play(SoundBuffer buffer, boolean doesLoop, float gain) {
        if (isPlaying())
            throw new IllegalStateException("Source is already playing");

        alSourcei(sourceId, AL_BUFFER, buffer.id());
        alSourcei(sourceId, AL_LOOPING, doesLoop ? 1 : 0);
        alSourcei(sourceId, AL_POSITION, 0);
        alSourcef(sourceId, AL_GAIN, gain);
        alSourcePlay(sourceId);
        isPlaying = true;
    }

    public void stop() {
        alSourceStop(sourceId);
        isPlaying = false;
    }

    public boolean isPlaying() {
        if (!isPlaying) return false;

        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state == AL_STOPPED) {
            isPlaying = false;
            return false;
        }

        return true;
    }

    public void delete() {
        alDeleteSources(sourceId);
    }
}
