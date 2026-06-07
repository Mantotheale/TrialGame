package com.game.window;

import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;

public enum OpenGLVersion {
    CORE_3_3(3, 3, GLFW_OPENGL_CORE_PROFILE, true);

    private final int major;
    private final int minor;
    private final int profile;
    private final boolean isForwardCompatible;

    OpenGLVersion(int major, int minor, int profile, boolean isForwardCompatible) {
        this.major = major;
        this.minor = minor;
        this.profile = profile;
        this.isForwardCompatible = isForwardCompatible;
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int profile() {
        return profile;
    }

    public boolean isForwardCompatible() {
        return isForwardCompatible;
    }
}
