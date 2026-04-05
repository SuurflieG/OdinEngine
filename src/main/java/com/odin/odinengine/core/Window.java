package com.odin.odinengine.core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private int width;
    private int height;
    private String title;

    private long windowHandle;
    private boolean resized;
    private boolean mouseCaptured = true;

    private double lastMouseX;
    private double lastMouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private boolean firstMouseInput = true;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.resized = false;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            glViewport(0, 0, newWidth, newHeight);
            resized = true;
        });

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            if (firstMouseInput) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouseInput = false;
            }

            mouseDeltaX = xpos - lastMouseX;
            mouseDeltaY = ypos - lastMouseY;

            lastMouseX = xpos;
            lastMouseY = ypos;
        });

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1);
        glfwShowWindow(windowHandle);

        setMouseCaptured(true);

        GL.createCapabilities();

        glViewport(0, 0, width, height);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public double getMouseDeltaX() {
        return mouseDeltaX;
    }

    public double getMouseDeltaY() {
        return mouseDeltaY;
    }

    public void resetMouseDelta() {
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
    }

    public void setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(windowHandle, title);
    }

    public void setMouseCaptured(boolean captured) {
        this.mouseCaptured = captured;
        glfwSetInputMode(windowHandle, GLFW_CURSOR, captured ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        firstMouseInput = true;
        resetMouseDelta();
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }

    public void cleanup() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();

        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public boolean isMouseButtonPressed(int button) {
        return glfwGetMouseButton(windowHandle, button) == GLFW_PRESS;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}