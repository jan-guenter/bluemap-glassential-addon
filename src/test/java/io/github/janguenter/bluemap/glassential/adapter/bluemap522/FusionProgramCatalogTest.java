/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.glassential.profile.Glassential345Fusion1312Profile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FusionProgramCatalogTest {

    @Test
    void parsesExactFusionAndOneWayPrograms() throws IOException {
        Map<String, byte[]> models = exactModels(required("glassentialJar"));
        FusionProgramCatalog catalog = parse(models);
        assertEquals(93, catalog.size());
        assertEquals(2, catalog.oneWaySize());

        long sameBlock = catalog.programs().values().stream()
                .filter(program -> program.predicate("unused")
                        instanceof FusionPredicate.SameBlock)
                .count();
        long sameState = catalog.programs().values().stream()
                .filter(program -> program.predicate("unused")
                        instanceof FusionPredicate.SameState)
                .count();
        assertEquals(36, sameBlock);
        assertEquals(55, sameState);

        for (Key key : Glassential345Fusion1312Profile.FUSION_MODEL_KEYS) {
            assertNotNull(catalog.get(key), () -> "missing Fusion program " + key);
        }
        assertEquals(Key.parse("minecraft:block/glass"), catalog.oneWayBase(
                Key.parse("glassential:block/one_way_glass")
        ));
        assertEquals(Key.parse("minecraft:block/tinted_glass"), catalog.oneWayBase(
                Key.parse("glassential:block/tinted_one_way_glass")
        ));

        FusionProgramCatalog.Program slab = catalog.get(
                Key.parse("glassential:block/glass_slab")
        );
        assertFalse(slab.predicate("side") instanceof FusionPredicate.Never);
        assertFalse(slab.predicate("bottom") instanceof FusionPredicate.Never);
        assertEquals(slab.predicate("bottom"), slab.predicate("top"));
    }

    @Test
    void rejectsAnyMissingClosureModel() throws IOException {
        Map<String, byte[]> models = exactModels(required("glassentialJar"));
        String removed = models.keySet().iterator().next();
        models.remove(removed);
        assertThrows(IllegalArgumentException.class, () -> parse(models));
    }

    private static FusionProgramCatalog parse(Map<String, byte[]> models) {
        return FusionProgramCatalog.parse(
                models,
                Glassential345Fusion1312Profile.FUSION_MODEL_KEYS,
                Glassential345Fusion1312Profile.ONE_WAY_MODEL_KEYS
        );
    }

    private static Map<String, byte[]> exactModels(Path jar) throws IOException {
        Map<String, byte[]> models = new HashMap<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Glassential345Fusion1312Profile.RESOURCES.entries().forEach((path, manifest) -> {
                if (!manifest.kind().equals("model")) {
                    return;
                }
                ZipEntry entry = zip.getEntry(path);
                assertNotNull(entry, path);
                try {
                    models.put(path, zip.getInputStream(entry).readAllBytes());
                } catch (IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
        } catch (java.io.UncheckedIOException exception) {
            throw exception.getCause();
        }
        assertEquals(100, models.size());
        assertTrue(models.keySet().stream().anyMatch(
                path -> path.startsWith("assets/glassential/models/")
        ));
        assertTrue(models.keySet().stream().anyMatch(
                path -> path.startsWith("assets/minecraft/models/")
        ));
        return models;
    }

    private static Path required(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new AssertionError("missing exact test artifact property: " + property);
        }
        return Path.of(value);
    }
}
