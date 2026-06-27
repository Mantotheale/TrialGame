package com.game.sound;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class SoundBuffer {
    private final int bufferId;

    public SoundBuffer(Path filePath) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channelsBuf = stack.mallocInt(1);
            IntBuffer sampleRateBuf = stack.mallocInt(1);
            ShortBuffer rawAudioBuf = stb_vorbis_decode_filename(filePath.toString(), channelsBuf, sampleRateBuf);
            if (rawAudioBuf == null)
                throw new IllegalStateException("Couldn't load sound " + filePath);

            int channels = channelsBuf.get();
            int sampleRate = sampleRateBuf.get();

            int format = switch (channels) {
                case 1 -> AL_FORMAT_MONO16;
                case 2 -> AL_FORMAT_STEREO16;
                default -> throw new IllegalStateException("Unsupported number of channels in audio file " + filePath + ". It had " + channels + " channels");
            };

            bufferId = alGenBuffers();
            alBufferData(bufferId, format, rawAudioBuf, sampleRate);

            free(rawAudioBuf);
        }
    }

    public int id() {
        return bufferId;
    }

    public void delete() {
        alDeleteBuffers(bufferId);
    }
}
