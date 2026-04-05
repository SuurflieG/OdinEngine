package com.odin.odinengine.core;

import com.odin.odinengine.math.Camera;
import com.odin.odinengine.render.Renderer;
import com.odin.odinengine.world.*;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {

    private Window window;
    private Renderer renderer;
    private Camera camera;
    private World world;
    private boolean running;

    private Vector3f debugRayStart;
    private Vector3f debugRayEnd;
    private Vector3f centerRayStart;
    private Vector3f centerRayEnd;
    private Vector3f offsetRayStart;
    private Vector3f offsetRayEnd;

    private RaycastHit currentRaycastHit;
    private static final float RAYCAST_MAX_DISTANCE = 6.0f;
    private static final float RAYCAST_STEP_SIZE = 0.05f;

    private BlockRegistry blockRegistry;

    private static final float MOVE_SPEED = 5.0f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final int LOAD_RADIUS = 2;

    private boolean debugOverlayEnabled = false;
    private boolean f3PressedLastFrame = false;
    private boolean tabPressedLastFrame = false;

    private boolean leftMousePressedLastFrame = false;
    private boolean rightMousePressedLastFrame = false;

    private int currentPlayerChunkX;
    private int currentPlayerChunkZ;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        window = new Window(1280, 720, "OdinEngine");
        window.init();

        camera = new Camera();
        camera.setPosition(16.0f, 14.0f, 52.0f);
        camera.setRotation(20.0f, 0.0f, 0.0f);

        currentPlayerChunkX = getPlayerChunkX();
        currentPlayerChunkZ = getPlayerChunkZ();

        blockRegistry = new BlockRegistry();
        blockRegistry.bootstrap();

        world = new World(blockRegistry);
        world.ensureChunksInRadius(currentPlayerChunkX, currentPlayerChunkZ, LOAD_RADIUS);

        renderer = new Renderer();
        renderer.init(1280, 720, world);

        running = true;
    }

    private void loop() {
        long lastTime = System.nanoTime();
        double fpsTimer = 0.0;
        int frames = 0;

        while (running && !window.shouldClose()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
            lastTime = currentTime;

            fpsTimer += deltaTime;
            frames++;

            update(deltaTime);
            render();
            window.update();

            if (fpsTimer >= 1.0) {
                updateWindowTitle(frames);
                frames = 0;
                fpsTimer = 0.0;
            }
        }
    }

    private void update(float deltaTime) {
        handleDebugToggle();
        handleMouseCaptureToggle();
        handleKeyboardInput(deltaTime);
        handleMouseInput();
        updateWorldStreaming();
        updateRaycast();
        handleBlockInteraction();
    }

    private void handleDebugToggle() {
        boolean f3CurrentlyPressed = window.isKeyPressed(GLFW_KEY_F3);

        if (f3CurrentlyPressed && !f3PressedLastFrame) {
            debugOverlayEnabled = !debugOverlayEnabled;
        }

        f3PressedLastFrame = f3CurrentlyPressed;
    }

    private void handleMouseCaptureToggle() {
        boolean tabCurrentlyPressed = window.isKeyPressed(GLFW_KEY_TAB);

        if (tabCurrentlyPressed && !tabPressedLastFrame) {
            window.setMouseCaptured(!window.isMouseCaptured());
        }

        tabPressedLastFrame = tabCurrentlyPressed;
    }

    private void handleKeyboardInput(float deltaTime) {
        float moveAmount = MOVE_SPEED * deltaTime;

        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(moveAmount);
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackward(moveAmount);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.strafeLeft(moveAmount);
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.strafeRight(moveAmount);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(moveAmount);
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveDown(moveAmount);
        }
    }

    private void handleMouseInput() {
        if (!window.isMouseCaptured()) {
            window.resetMouseDelta();
            return;
        }

        float yawDelta = (float) window.getMouseDeltaX() * MOUSE_SENSITIVITY;
        float pitchDelta = (float) window.getMouseDeltaY() * MOUSE_SENSITIVITY;

        camera.addRotation(-pitchDelta, -yawDelta);
        window.resetMouseDelta();
    }

    private void handleBlockInteraction() {
        boolean leftCurrentlyPressed = window.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT);
        boolean rightCurrentlyPressed = window.isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT);

        if (window.isMouseCaptured() && currentRaycastHit != null) {
            if (leftCurrentlyPressed && !leftMousePressedLastFrame) {
                breakTargetedBlock();
            }

            if (rightCurrentlyPressed && !rightMousePressedLastFrame) {
                placeBlockAtTargetFace();
            }
        }

        leftMousePressedLastFrame = leftCurrentlyPressed;
        rightMousePressedLastFrame = rightCurrentlyPressed;
    }

    private void breakTargetedBlock() {
        int blockX = currentRaycastHit.getBlockX();
        int blockY = currentRaycastHit.getBlockY();
        int blockZ = currentRaycastHit.getBlockZ();

        world.setBlockId(blockX, blockY, blockZ, BlockRegistry.AIR_ID);

        renderer.rebuildChunks(world, getAffectedChunksForBlockEdit(blockX, blockY, blockZ));
        updateRaycast();
    }

    private void placeBlockAtTargetFace() {
        int placeX = currentRaycastHit.getPlaceX();
        int placeY = currentRaycastHit.getPlaceY();
        int placeZ = currentRaycastHit.getPlaceZ();

        if (placeY < 0 || placeY >= Chunk.SIZE_Y) {
            return;
        }

        if (world.isBlockSolid(placeX, placeY, placeZ)) {
            return;
        }

        short blockToPlace = blockRegistry.getId("grass");
        world.setBlockId(placeX, placeY, placeZ, blockToPlace);

        renderer.rebuildChunks(world, getAffectedChunksForBlockEdit(placeX, placeY, placeZ));
        updateRaycast();
    }

    private void updateWorldStreaming() {
        int playerChunkX = getPlayerChunkX();
        int playerChunkZ = getPlayerChunkZ();

        if (playerChunkX != currentPlayerChunkX || playerChunkZ != currentPlayerChunkZ) {
            currentPlayerChunkX = playerChunkX;
            currentPlayerChunkZ = playerChunkZ;

            world.ensureChunksInRadius(currentPlayerChunkX, currentPlayerChunkZ, LOAD_RADIUS);
            world.unloadChunksOutsideRadius(currentPlayerChunkX, currentPlayerChunkZ, LOAD_RADIUS);
            renderer.rebuildWorldMeshes(world);
        }
    }

    private void updateRaycast() {
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f forward = new Vector3f(camera.getForwardVector()).normalize();

        currentRaycastHit = world.raycast(
                origin,
                forward,
                RAYCAST_MAX_DISTANCE
        );

        float visibleLength = 6.0f;

        centerRayStart = new Vector3f(origin).add(new Vector3f(forward).mul(0.2f));
        centerRayEnd = new Vector3f(centerRayStart).add(new Vector3f(forward).mul(visibleLength));

        Vector3f worldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f right = new Vector3f(forward).cross(worldUp);

        if (right.lengthSquared() < 0.0001f) {
            right.set(1.0f, 0.0f, 0.0f);
        } else {
            right.normalize();
        }

        offsetRayStart = new Vector3f(centerRayStart).add(new Vector3f(right).mul(0.15f));
        offsetRayEnd = new Vector3f(centerRayEnd).add(new Vector3f(right).mul(0.15f));
    }

    private Set<ChunkPos> getAffectedChunksForBlockEdit(int worldX, int worldY, int worldZ) {
        Set<ChunkPos> affected = new HashSet<>();

        ChunkPos center = world.getChunkPosFromWorldPos(worldX, worldZ);
        affected.add(center);

        int localX = Math.floorMod(worldX, Chunk.SIZE_X);
        int localZ = Math.floorMod(worldZ, Chunk.SIZE_Z);

        if (localX == 0) {
            affected.add(new ChunkPos(center.x() - 1, center.z()));
        }
        if (localX == Chunk.SIZE_X - 1) {
            affected.add(new ChunkPos(center.x() + 1, center.z()));
        }
        if (localZ == 0) {
            affected.add(new ChunkPos(center.x(), center.z() - 1));
        }
        if (localZ == Chunk.SIZE_Z - 1) {
            affected.add(new ChunkPos(center.x(), center.z() + 1));
        }

        return affected;
    }

    private int getPlayerChunkX() {
        return Math.floorDiv((int) Math.floor(camera.getPosition().x), Chunk.SIZE_X);
    }

    private int getPlayerChunkZ() {
        return Math.floorDiv((int) Math.floor(camera.getPosition().z), Chunk.SIZE_Z);
    }

    private void render() {
        String targetedBlockName = null;
        boolean showBlockOutline = false;
        int hitBlockX = 0;
        int hitBlockY = 0;
        int hitBlockZ = 0;

        boolean showHitPoint = false;
        float hitX = 0.0f;
        float hitY = 0.0f;
        float hitZ = 0.0f;

        if (currentRaycastHit != null) {
            targetedBlockName = world.getBlockRegistry()
                    .get(currentRaycastHit.getBlockId())
                    .getName()
                    .toUpperCase();

            showBlockOutline = true;
            hitBlockX = currentRaycastHit.getBlockX();
            hitBlockY = currentRaycastHit.getBlockY();
            hitBlockZ = currentRaycastHit.getBlockZ();

            showHitPoint = true;
            hitX = currentRaycastHit.getHitX();
            hitY = currentRaycastHit.getHitY();
            hitZ = currentRaycastHit.getHitZ();
        }

        renderer.render(
                camera,
                world,
                targetedBlockName,
                window.getWidth(),
                window.getHeight(),
                centerRayStart,
                centerRayEnd,
                offsetRayStart,
                offsetRayEnd,
                showBlockOutline,
                hitBlockX,
                hitBlockY,
                hitBlockZ,
                showHitPoint,
                hitX,
                hitY,
                hitZ
        );
    }

    private void updateWindowTitle(int fps) {
        if (debugOverlayEnabled) {
            int renderedBlocks = renderer.getRenderedBlockCount();
            int renderedFaces = renderer.getRenderedFaceCount();

            String targetInfo;
            if (currentRaycastHit != null) {
                String blockName = world.getBlockRegistry().get(currentRaycastHit.getBlockId()).getName();
                targetInfo = String.format(
                        " | Target: %s @ (%d, %d, %d)",
                        blockName,
                        currentRaycastHit.getBlockX(),
                        currentRaycastHit.getBlockY(),
                        currentRaycastHit.getBlockZ()
                );
            } else {
                targetInfo = " | Target: none";
            }

            String debugTitle = String.format(
                    "OdinEngine | FPS: %d | Chunks: %d | Blocks: %d | Faces: %d | Pos: (%.2f, %.2f, %.2f)%s",
                    fps,
                    world.getLoadedChunkCount(),
                    renderedBlocks,
                    renderedFaces,
                    camera.getPosition().x,
                    camera.getPosition().y,
                    camera.getPosition().z,
                    targetInfo
            );

            window.setTitle(debugTitle);
        } else {
            window.setTitle("OdinEngine");
        }
    }

    private void cleanup() {
        renderer.cleanup();
        window.cleanup();
    }
}