package com.odin.odinengine.world;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class BlockLoader {

    private static final Gson GSON = new Gson();

    public BlockDefinitionFile load(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find block definition: " + resourcePath);
            }

            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                BlockDefinitionFile file = GSON.fromJson(reader, BlockDefinitionFile.class);

                if (file == null) {
                    throw new RuntimeException("Parsed block definition was null: " + resourcePath);
                }

                return file;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load block definition from: " + resourcePath, e);
        }
    }
}