package com.game.sound;

import org.lwjgl.openal.*;

import java.nio.IntBuffer;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundDevice {
    private final long device;
    private final long context;

    public SoundDevice() {
        device = alcOpenDevice((String) null);
        if (device == NULL)
            throw new IllegalStateException("Failed to open an OpenAL device.");

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        if (!alcCapabilities.OpenALC10)
            throw new IllegalStateException("The found sound device doesn't support OpenAL 1.0");

        context = alcCreateContext(device, (IntBuffer)null);
        checkALCError(device);

        if (!alcMakeContextCurrent(context))
            throw new IllegalStateException("Failed to make OpenAL context current");
        checkALCError(device);

        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
        if (!alCapabilities.OpenAL10)
            throw new IllegalStateException("The sound context doesn't support OpenAL 1.0");
    }

    public void delete() {
        alcMakeContextCurrent(NULL);
        AL.setCurrentProcess(null);
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    private static void checkALCError(long device) {
        int err = alcGetError(device);
        if (err != ALC_NO_ERROR)
            throw new RuntimeException(alcGetString(device, err));
    }
}
