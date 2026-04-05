package com.odin.odinengine.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {

    public static final short AIR_ID = 0;

    private final Map<Short, BlockDefinition> blocksById = new HashMap<>();
    private final Map<String, BlockDefinition> blocksByName = new HashMap<>();
    private final BlockLoader blockLoader = new BlockLoader();

    private short nextId = 1;

    public void bootstrap() {
        registerInternal(new BlockDefinition(AIR_ID, "air", false, null, null, null, null));

        register("grass");
        register("dirt");
        register("stone");
    }

    public void register(String blockName) {
        String jsonPath = "assets/odinengine/json/blocks/" + blockName + ".json";
        BlockDefinitionFile file = blockLoader.load(jsonPath);

        validateBlockDefinitionFile(file, jsonPath);

        short id = nextId++;
        BlockDefinition definition = new BlockDefinition(
                id,
                file.getName(),
                file.isSolid(),
                file.getTextures().getAll(),
                file.getTextures().getTop(),
                file.getTextures().getBottom(),
                file.getTextures().getSide()
        );

        registerInternal(definition);
    }

    private void validateBlockDefinitionFile(BlockDefinitionFile file, String jsonPath) {
        if (file.getName() == null || file.getName().isBlank()) {
            throw new IllegalStateException("Block JSON missing valid 'name': " + jsonPath);
        }

        if (file.getTextures() == null) {
            throw new IllegalStateException("Block JSON missing 'textures' object: " + jsonPath);
        }

        boolean hasAll = file.getTextures().getAll() != null && !file.getTextures().getAll().isBlank();
        boolean hasPerFace =
                file.getTextures().getTop() != null && !file.getTextures().getTop().isBlank() &&
                        file.getTextures().getBottom() != null && !file.getTextures().getBottom().isBlank() &&
                        file.getTextures().getSide() != null && !file.getTextures().getSide().isBlank();

        if (!hasAll && !hasPerFace) {
            throw new IllegalStateException(
                    "Block JSON must define either textures.all or textures.top + textures.bottom + textures.side: " + jsonPath
            );
        }
    }

    private void registerInternal(BlockDefinition definition) {
        if (blocksById.containsKey(definition.getId())) {
            throw new IllegalStateException("Duplicate block ID: " + definition.getId());
        }

        if (blocksByName.containsKey(definition.getName())) {
            throw new IllegalStateException("Duplicate block name: " + definition.getName());
        }

        blocksById.put(definition.getId(), definition);
        blocksByName.put(definition.getName(), definition);
    }

    public BlockDefinition get(short id) {
        BlockDefinition definition = blocksById.get(id);
        if (definition == null) {
            throw new IllegalStateException("Unknown block ID: " + id);
        }
        return definition;
    }

    public BlockDefinition get(String name) {
        BlockDefinition definition = blocksByName.get(name);
        if (definition == null) {
            throw new IllegalStateException("Unknown block name: " + name);
        }
        return definition;
    }

    public short getId(String name) {
        return get(name).getId();
    }

    public boolean isSolid(short id) {
        return get(id).isSolid();
    }

    public Collection<BlockDefinition> getAll() {
        return blocksById.values();
    }
}