package com.game;

import com.game.renderer.indexbuffer.IndexBuffer;
import com.game.renderer.shaderprogram.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.renderer.vertexarray.VertexArray;
import com.game.renderer.vertexbuffer.VertexBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    private final static double ONE_SEC_TIME = 1;
    private final static double UPDATE_TIME = 1d / 60;

    private final long window;
    private int width;
    private int height;

    private final ShaderProgram shaderProgram;
    private final VertexArray vao;
    private final VertexBuffer vbo;
    private final IndexBuffer ebo;
    private final Texture texture1;
    private final Texture texture2;
    private final Matrix4f model1;
    private final Matrix4f model2;
    private final Matrix4f view;
    private final Matrix4f projection;
    private final Vector3f worldUp;
    private final Vector3f cameraPosition;
    private final Vector3f cameraTarget;


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

        vbo = VertexBuffer.create(glGenBuffers());
        glBindBuffer(GL_ARRAY_BUFFER, vbo.id());
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int[] indices = {
            0, 1, 3,
            1, 2, 3
        };

        ebo = IndexBuffer.create(glGenBuffers());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo.id());
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        vao = VertexArray.create(glGenVertexArrays());
        glBindVertexArray(vao.id());
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        texture1 = Texture.create(glGenTextures());
        glBindTexture(GL_TEXTURE_2D, texture1.id());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = stbi_load("src/main/resources/images/reshiram.png", width, height, channels, 4);
            if (image == null) { throw new RuntimeException("Couldn't open image"); }

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(image);
        }

        texture2 = Texture.create(glGenTextures());
        glBindTexture(GL_TEXTURE_2D, texture2.id());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = stbi_load("src/main/resources/images/background.png", width, height, channels, 4);
            if (image == null) { throw new RuntimeException("Couldn't open image"); }

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(image);
        }

        String vertexShaderSource = """
                #version 330
                
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec2 aTexCoord;
                
                out vec2 texCoord;
                
                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
                
                void main() {
                    gl_Position = projection * view * model * vec4(aPos, 1.0);
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

        shaderProgram = ShaderProgram.create(glCreateProgram());
        glAttachShader(shaderProgram.id(), vertexShader);
        glAttachShader(shaderProgram.id(), fragmentShader);
        glLinkProgram(shaderProgram.id());
        if (glGetProgrami(shaderProgram.id(), GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't link the shader program");
        }

        glDetachShader(shaderProgram.id(), vertexShader);
        glDetachShader(shaderProgram.id(), fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        model1 = new Matrix4f().translate(0, 0, 1);
        worldUp = new Vector3f(0, 1, 0);
        cameraPosition = new Vector3f(0, 0, 20);
        cameraTarget =  new Vector3f(0);
        view = new Matrix4f().lookAt(cameraPosition, cameraTarget, worldUp);
        projection = new Matrix4f().ortho(-5, 5, -5, 5, 0.01f, 20);

        model2 = new Matrix4f().scale(10);

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
            model1.translate(0, 1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            model1.translate(0, -1 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            model1.translate(-1 * (float) UPDATE_TIME, 0, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            model1.translate(1 * (float) UPDATE_TIME, 0, 0);
        }

        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            cameraPosition.add(0, 2 * (float) UPDATE_TIME, 0);
            cameraTarget.add(0, 2 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            cameraPosition.sub(0, 2 * (float) UPDATE_TIME, 0);
            cameraTarget.sub(0, 2 * (float) UPDATE_TIME, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            cameraPosition.sub(2 * (float) UPDATE_TIME, 0, 0);
            cameraTarget.sub(2 * (float) UPDATE_TIME, 0, 0);
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            cameraPosition.add(2 * (float) UPDATE_TIME, 0, 0);
            cameraTarget.add(2 * (float) UPDATE_TIME, 0, 0);
        }
        view.setLookAt(cameraPosition, cameraTarget, worldUp);
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

        glUseProgram(shaderProgram.id());
        glBindVertexArray(vao.id());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo.id());
        glBindTexture(GL_TEXTURE_2D, texture2.id());

        int modelLocation2 = glGetUniformLocation(shaderProgram.id(), "model");
        if (modelLocation2 == -1)
            throw new RuntimeException("Couldn't locate model uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  model2.get(stack.mallocFloat(16));
            glUniformMatrix4fv(modelLocation2, false, buffer);
        }

        int viewLocation2 = glGetUniformLocation(shaderProgram.id(), "view");
        if (viewLocation2 == -1)
            throw new RuntimeException("Couldn't locate view uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  view.get(stack.mallocFloat(16));
            glUniformMatrix4fv(viewLocation2, false, buffer);
        }

        int projectionLocation2 = glGetUniformLocation(shaderProgram.id(), "projection");
        if (projectionLocation2 == -1)
            throw new RuntimeException("Couldn't locate projection uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  projection.get(stack.mallocFloat(16));
            glUniformMatrix4fv(projectionLocation2, false, buffer);
        }

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glUseProgram(shaderProgram.id());
        glBindVertexArray(vao.id());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo.id());
        glBindTexture(GL_TEXTURE_2D, texture1.id());

        int modelLocation1 = glGetUniformLocation(shaderProgram.id(), "model");
        if (modelLocation1 == -1)
            throw new RuntimeException("Couldn't locate model uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  model1.get(stack.mallocFloat(16));
            glUniformMatrix4fv(modelLocation1, false, buffer);
        }

        int viewLocation1 = glGetUniformLocation(shaderProgram.id(), "view");
        if (viewLocation1 == -1)
            throw new RuntimeException("Couldn't locate view uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  view.get(stack.mallocFloat(16));
            glUniformMatrix4fv(viewLocation1, false, buffer);
        }

        int projectionLocation1 = glGetUniformLocation(shaderProgram.id(), "projection");
        if (projectionLocation1 == -1)
            throw new RuntimeException("Couldn't locate projection uniform");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer =  projection.get(stack.mallocFloat(16));
            glUniformMatrix4fv(projectionLocation1, false, buffer);
        }

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glfwSwapBuffers(window);
    }

    private boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    private void terminate() {
        glDeleteVertexArrays(vao.id());
        glDeleteBuffers(vbo.id());
        glDeleteBuffers(ebo.id());
        glDeleteTextures(texture1.id());
        glDeleteTextures(texture2.id());
        glDeleteProgram(shaderProgram.id());

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
