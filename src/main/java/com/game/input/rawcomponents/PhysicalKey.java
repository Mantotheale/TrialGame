package com.game.input.rawcomponents;

import static org.lwjgl.glfw.GLFW.*;

public enum PhysicalKey {
    A(GLFW_KEY_A),
    B(GLFW_KEY_B),
    C(GLFW_KEY_C),
    D(GLFW_KEY_D),
    E(GLFW_KEY_E),
    F(GLFW_KEY_F),
    G(GLFW_KEY_G),
    H(GLFW_KEY_H),
    I(GLFW_KEY_I),
    J(GLFW_KEY_J),
    K(GLFW_KEY_K),
    L(GLFW_KEY_L),
    M(GLFW_KEY_M),
    N(GLFW_KEY_N),
    O(GLFW_KEY_O),
    P(GLFW_KEY_P),
    Q(GLFW_KEY_Q),
    R(GLFW_KEY_R),
    S(GLFW_KEY_S),
    T(GLFW_KEY_T),
    U(GLFW_KEY_U),
    V(GLFW_KEY_V),
    W(GLFW_KEY_W),
    X(GLFW_KEY_X),
    Y(GLFW_KEY_Y),
    Z(GLFW_KEY_Z),
    SPACE(GLFW_KEY_SPACE),
    ENTER(GLFW_KEY_ENTER),
    BACKSPACE(GLFW_KEY_BACKSPACE),
    ESCAPE(GLFW_KEY_ESCAPE),
    TAB(GLFW_KEY_TAB),
    LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
    RIGHT_SHIFT(GLFW_KEY_RIGHT_SHIFT),
    LEFT_CONTROL(GLFW_KEY_LEFT_CONTROL),
    RIGHT_CONTROL(GLFW_KEY_RIGHT_CONTROL),
    DELETE(GLFW_KEY_DELETE),
    LEFT_ALT(GLFW_KEY_LEFT_ALT),
    RIGHT_ALT(GLFW_KEY_RIGHT_ALT),
    UP(GLFW_KEY_UP),
    DOWN(GLFW_KEY_DOWN),
    LEFT(GLFW_KEY_LEFT),
    RIGHT(GLFW_KEY_RIGHT),
    UNMAPPED(GLFW_KEY_UNKNOWN);

    private final int glfwCode;

    PhysicalKey(int glfwCode) {
        this.glfwCode = glfwCode;
    }

    public static PhysicalKey fromGlfwCode(int glfwCode) {
        if (glfwCode == GLFW_KEY_UNKNOWN) return PhysicalKey.UNMAPPED;
        if (glfwCode < 0 || glfwCode > GLFW_KEY_LAST) throw new IllegalArgumentException("Invalid glfw key code: " + glfwCode);

        return GLFW_MAPPING[glfwCode];
    }

    private final static PhysicalKey[] GLFW_MAPPING = initializeGLFWMapping();

    private static PhysicalKey[] initializeGLFWMapping() {
        PhysicalKey[] mapping = new PhysicalKey[GLFW_KEY_LAST + 1];

        for (PhysicalKey key : PhysicalKey.values())
            if (key != UNMAPPED)
                mapping[key.glfwCode] = key;

        for (int i = 0; i < mapping.length; i++)
            if (mapping[i] == null)
                mapping[i] = PhysicalKey.UNMAPPED;

        return mapping;
    }
}
