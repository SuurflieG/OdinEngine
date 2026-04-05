package com.odin.odinengine.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Vector3f rotation;

    public Camera() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateY((float) Math.toRadians(-rotation.y))
                .rotateZ((float) Math.toRadians(-rotation.z))
                .translate(-position.x, -position.y, -position.z);
    }

    public void moveForward(float amount) {
        position.x -= (float) Math.sin(Math.toRadians(rotation.y)) * amount;
        position.z -= (float) Math.cos(Math.toRadians(rotation.y)) * amount;
    }

    public void moveBackward(float amount) {
        moveForward(-amount);
    }

    public void strafeLeft(float amount) {
        position.x -= (float) Math.sin(Math.toRadians(rotation.y + 90.0f)) * amount;
        position.z -= (float) Math.cos(Math.toRadians(rotation.y + 90.0f)) * amount;
    }

    public void strafeRight(float amount) {
        strafeLeft(-amount);
    }

    public void moveUp(float amount) {
        position.y += amount;
    }

    public void moveDown(float amount) {
        position.y -= amount;
    }

    public void addRotation(float pitchDelta, float yawDelta) {
        rotation.x += pitchDelta;
        rotation.y += yawDelta;

        if (rotation.x > 89.0f) {
            rotation.x = 89.0f;
        }
        if (rotation.x < -89.0f) {
            rotation.x = -89.0f;
        }
    }

    public Vector3f getForwardVector() {
        Vector3f forward = new Vector3f();

        float pitchRad = (float) Math.toRadians(rotation.x);
        float yawRad = (float) Math.toRadians(rotation.y);

        forward.x = -(float) (Math.sin(yawRad) * Math.cos(pitchRad));
        forward.y = (float) Math.sin(pitchRad);
        forward.z = -(float) (Math.cos(yawRad) * Math.cos(pitchRad));

        return forward.normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
    }
}