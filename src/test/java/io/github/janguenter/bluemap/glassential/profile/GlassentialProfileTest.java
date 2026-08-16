/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlassentialProfileTest {

    @Test
    void locksRouteAndLegalStateCensus() {
        Map<ShapeFamily, Long> shapes = Glassential345Fusion1312Profile.DEFINITIONS.values()
                .stream()
                .collect(Collectors.groupingBy(
                        GlassentialDefinition::shape,
                        () -> new EnumMap<>(ShapeFamily.class),
                        Collectors.counting()
                ));
        assertEquals(Map.of(
                ShapeFamily.FULL, 35L,
                ShapeFamily.PANE, 11L,
                ShapeFamily.SLAB, 1L,
                ShapeFamily.ONE_WAY, 2L
        ), shapes);
        Map<StateSchema, Long> schemas = Glassential345Fusion1312Profile.DEFINITIONS.values()
                .stream()
                .collect(Collectors.groupingBy(
                        GlassentialDefinition::stateSchema,
                        () -> new EnumMap<>(StateSchema.class),
                        Collectors.counting()
                ));
        assertEquals(Map.of(
                StateSchema.PROPERTYLESS, 33L,
                StateSchema.PANE, 9L,
                StateSchema.COLORABLE, 2L,
                StateSchema.COLORABLE_PANE, 2L,
                StateSchema.SLAB, 1L,
                StateSchema.ONE_WAY, 2L
        ), schemas);
        int legalStates = Glassential345Fusion1312Profile.DEFINITIONS.values()
                .stream().mapToInt(GlassentialDefinition::legalStates).sum();
        assertEquals(471, legalStates);
        assertEquals(49, Glassential345Fusion1312Profile.ROUTED_BLOCKS.size());
        assertEquals(31, Glassential345Fusion1312Profile.ROUTED_GLASSENTIAL_BLOCK_IDS.size());
        assertEquals(18, Glassential345Fusion1312Profile.VANILLA_OVERRIDE_BLOCK_IDS.size());
        assertEquals(31, Glassential345Fusion1312Profile.ROUTED_BLOCKS.stream()
                .filter(id -> id.startsWith("glassential:"))
                .count());
        assertEquals(18, Glassential345Fusion1312Profile.ROUTED_BLOCKS.stream()
                .filter(id -> id.startsWith("minecraft:"))
                .count());
        assertFalse(Glassential345Fusion1312Profile.ROUTED_BLOCKS.stream()
                .anyMatch(id -> id.startsWith("fusion:")));
    }

    @Test
    void locksDirectProgramsAndInstalledResourceClosure() {
        assertEquals(95, Glassential345Fusion1312Profile.DIRECT_MODEL_KEYS.size());
        assertEquals(93, Glassential345Fusion1312Profile.FUSION_MODEL_KEYS.size());
        assertEquals(2, Glassential345Fusion1312Profile.ONE_WAY_MODEL_KEYS.size());
        assertEquals(2, Glassential345Fusion1312Profile.ONE_WAY_BLOCK_IDS.size());
        assertEquals(4, Glassential345Fusion1312Profile.COLORABLE_BLOCK_IDS.size());

        Map<String, Long> resources = Glassential345Fusion1312Profile.RESOURCES.entries()
                .values().stream()
                .collect(Collectors.groupingBy(
                        ResourceManifest.Entry::kind, Collectors.counting()
                ));
        assertEquals(Map.of(
                "blockstate", 31L,
                "model", 100L,
                "texture", 39L,
                "metadata", 34L
        ), resources);
        assertEquals(204, Glassential345Fusion1312Profile.RESOURCES.entries().size());
        assertEquals(22, Glassential345Fusion1312Profile.HOST_RESOURCES.entries().size());
        assertTrue(Glassential345Fusion1312Profile.RESOURCES.entries().keySet().stream()
                .allMatch(path -> path.startsWith("assets/glassential/")
                        || path.startsWith("assets/minecraft/models/block/")));
    }

    @Test
    void locksTextureLayoutAndHostAbiCensus() {
        assertEquals(48, TextureLayout.FULL.columns() * TextureLayout.FULL.rows());
        assertEquals(8, TextureLayout.FULL.physicalRows());
        Map<TextureLayout, Long> layouts = Glassential345Fusion1312Profile.TEXTURES.entries()
                .values().stream()
                .collect(Collectors.groupingBy(
                        TextureCatalog.Entry::layout,
                        () -> new EnumMap<>(TextureLayout.class),
                        Collectors.counting()
                ));
        assertEquals(Map.of(
                TextureLayout.PLAIN, 5L,
                TextureLayout.FULL, 22L,
                TextureLayout.SIMPLE, 9L,
                TextureLayout.PIECED, 3L
        ), layouts);
        Glassential345Fusion1312Profile.TEXTURES.entries().forEach((key, entry) -> {
            assertTrue(key.getFormatted().startsWith("glassential:"));
            assertEquals(16, entry.width() / entry.layout().columns());
            assertEquals(16, entry.height() / entry.layout().physicalRows());
            if (entry.layout() == TextureLayout.PLAIN) {
                assertEquals("-", entry.metadataSha256());
            } else {
                assertTrue(entry.metadataSha256().matches("[0-9a-f]{64}"));
            }
        });

        Map<String, Long> host = Glassential345Fusion1312Profile.HOST_RESOURCES.entries()
                .values().stream()
                .collect(Collectors.groupingBy(
                        ResourceManifest.Entry::kind, Collectors.counting()
                ));
        assertEquals(Map.of("blockstate", 18L, "model", 3L, "texture", 1L), host);
        assertTrue(Glassential345Fusion1312Profile.HOST_RESOURCES.entries().containsKey(
                "assets/minecraft/textures/block/glass_pane_top.png"
        ));
    }
}
