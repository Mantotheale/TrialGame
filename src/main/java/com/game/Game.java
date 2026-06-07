package com.game;

import com.game.camera.Camera;
import com.game.camera.CameraProjection;
import com.game.renderer.texture.Texture;
import com.game.renderer.texture.TextureAttributes;
import com.game.renderer.texture.TextureMagnifyingFilter;
import com.game.renderer.texture.TextureMinifyingFilter;
import com.game.transform.Rotation;
import com.game.transform.Scale;
import com.game.transform.Transform;
import com.game.transform.Translation;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    private final static double ONE_SEC_TIME = 1;
    private final static double UPDATE_TIME = 1d / 60;

    private final long window;
    private int width;
    private int height;

    private final int shaderProgram;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final Texture texture1;
    private final Texture texture2;
    private Transform transform1;
    private Transform transform2;
    private final Camera camera;

    private int updates;
    private int frames;

    public Game() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        width = 720;
        height = 720;
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, _, action, _) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetFramebufferSizeCallback(window, (_, width, height) -> {
            this.width = width;
            this.height = height;

            glViewport(0, 0, width, height);
        });

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            assert vidmode != null;
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(0.957f, 0.9062f, 0.5859f, 1.0f);

        float[] vertices = {
            -0.5f, -0.5f, 0, 0, 0,
            0.5f, -0.5f, 0, 1, 0,
            0.5f, 0.5f, 0, 1, 1,
            -0.5f, 0.5f, 0, 0, 1
        };

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int[] indices = {
            0, 1, 3,
            1, 2, 3
        };

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        TextureAttributes texAttr = new TextureAttributes.Builder()
                .magnifyingFilter(TextureMagnifyingFilter.LINEAR)
                .minifyingFilter(TextureMinifyingFilter.LINEAR_MIPMAP_LINEAR)
                .mipmap(true)
                .build();

        texture1 = new Texture(texAttr, Path.of("src/main/resources/images/reshiram.png"));
        texture2 = new Texture(texAttr, Path.of("src/main/resources/images/background.png"));

        String vertexShaderSource = """
                #version 330
                
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec2 aTexCoord;
                
                out vec2 texCoord;
                
                uniform mat4 model;
                uniform mat4 viewProjection;
                //uniform mat4 view;
                //uniform mat4 projection;
                
                void main() {
                    gl_Position = viewProjection * model * vec4(aPos, 1.0);
                    //gl_Position = projection * view * model * vec4(aPos, 1.0);
                    texCoord = aTexCoord;
                }
                """;
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't compile vertex the shader");
        }

        String fragmentShaderSource = """
                #version 330
                
                out vec4 FragColor;
                
                in vec2 texCoord;
                
                uniform sampler2D tex;
                
                void main() {
                    FragColor = texture(tex, texCoord);
                }
                """;
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't compile the fragment shader");
        }

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't link the shader program");
        }

        glDetachShader(shaderProgram, vertexShader);
        glDetachShader(shaderProgram, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        camera = new Camera(
                new CameraProjection.Orthographic(-5, 5, -5, 5, 0.01f, 20),
                new Transform(new Translation(0, 0, 20), Rotation.fromDirection(Rotation.WORLD_FRONT), new Scale())
        );

        transform1 = new Transform(new Translation(0, 0, 1), new Rotation(), new Scale());
        transform2 = new Transform(new Translation(), new Rotation(), new Scale(10));

        updates = 0;
        frames = 0;
    }

    public void run() {
        double currentTime = glfwGetTime();
        double nextUpdateTime = currentTime + UPDATE_TIME;
        double nextOneSecTime = currentTime + ONE_SEC_TIME;

        while (!shouldClose()) {
            processInputs();

            currentTime = glfwGetTime();
            while (currentTime >= nextUpdateTime) {
                update();
                nextUpdateTime += UPDATE_TIME;
            }

            render();

            while (currentTime >= nextOneSecTime) {
                oneSecUpdate();
                nextOneSecTime += ONE_SEC_TIME;
            }
        }

        terminate();
    }

    private void processInputs() {
        glfwPollEvents();
    }

    private void update() {
        updates++;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            transform1 = transform1.translate(0, 1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            transform1 = transform1.translate(0, -1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            transform1 = transform1.translate(-1 * (float) UPDATE_TIME, 0, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            transform1 = transform1.translate(1 * (float) UPDATE_TIME, 0, 0);
        }

        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            camera.move(new Translation(0, 2 * (float) UPDATE_TIME, 0));
        }
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            camera.move(new Translation(0, -2 * (float) UPDATE_TIME, 0));
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            camera.move(new Translation(-2 * (float) UPDATE_TIME, 0, 0));
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            camera.move(new Translation(2 * (float) UPDATE_TIME, 0, 0));
        }
    }

    private void oneSecUpdate() {
        System.out.println("UPS: " + updates);
        System.out.println("FPS: " + frames);
        updates = 0;
        frames = 0;
    }

    private void render() {
        frames++;

        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBindTexture(GL_TEXTURE_2D, texture2.id());

        int modelLocation2 = glGetUniformLocation(shaderProgram, "model");
        if (modelLocation2 == -1)
            throw new RuntimeException("Couldn't locate model uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  transform2.matrix().get(stack.mallocFloat(16));
            glUniformMatrix4fv(modelLocation2, false, buffer);
        }

        int viewProjectionLocation2 = glGetUniformLocation(shaderProgram, "viewProjection");
        if (viewProjectionLocation2 == -1)
            throw new RuntimeException("Couldn't locate view uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  camera.matrix().get(stack.mallocFloat(16));
            glUniformMatrix4fv(viewProjectionLocation2, false, buffer);
        }

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glUseProgram(shaderProgram);
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBindTexture(GL_TEXTURE_2D, texture1.id());

        int modelLocation1 = glGetUniformLocation(shaderProgram, "model");
        if (modelLocation1 == -1)
            throw new RuntimeException("Couldn't locate model uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  transform1.matrix().get(stack.mallocFloat(16));
            glUniformMatrix4fv(modelLocation1, false, buffer);
        }

        int viewProjectionLocation1 = glGetUniformLocation(shaderProgram, "viewProjection");
        if (viewProjectionLocation1 == -1)
            throw new RuntimeException("Couldn't locate viewProjection uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  camera.matrix().get(stack.mallocFloat(16));
            glUniformMatrix4fv(viewProjectionLocation1, false, buffer);
        }

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glfwSwapBuffers(window);
    }

    private boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    private void terminate() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        texture1.delete();
        texture2.delete();
        glDeleteProgram(shaderProgram);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
