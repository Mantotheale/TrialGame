package com.game.sound;

public class SoundRequestId {
    private static int counter = 1;

    private final int id;

    private SoundRequestId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof SoundRequestId other) && this.id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SoundId(" + id + ")";
    }

    public static SoundRequestId generateId() {
        SoundRequestId id = new SoundRequestId(counter);
        counter++;
        return id;
    }

    public final static SoundRequestId NONE = new SoundRequestId(0);
}
