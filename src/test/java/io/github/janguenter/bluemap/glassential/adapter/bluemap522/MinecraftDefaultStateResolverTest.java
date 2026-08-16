/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftDefaultStateResolverTest {

    @Test
    void calibrationLocksExactMinecraft1211Defaults() {
        BlockState grass = state("minecraft:grass_block", Map.of("snowy", "false"));
        BlockState leaves = state("minecraft:oak_leaves", Map.of(
                "distance", "7",
                "persistent", "false",
                "waterlogged", "false"
        ));
        BlockState iron = state("minecraft:iron_block", Map.of());

        assertTrue(MinecraftDefaultStateResolver.knownDefaultsMatch(
                grass, leaves, iron
        ));
        assertFalse(MinecraftDefaultStateResolver.knownDefaultsMatch(
                state("minecraft:grass_block", Map.of("snowy", "true")),
                leaves,
                iron
        ));
        assertFalse(MinecraftDefaultStateResolver.knownDefaultsMatch(
                grass,
                state("minecraft:oak_leaves", Map.of(
                        "distance", "1",
                        "persistent", "false",
                        "waterlogged", "false"
                )),
                iron
        ));
    }

    @Test
    void mimicSelectionSeparatesRegisteredUnknownAndRecursiveTargets() {
        BlockState grass = state("minecraft:grass_block", Map.of("snowy", "false"));
        BlockState iron = state("minecraft:iron_block", Map.of());
        Map<Key, BlockState> registered = Map.of(
                grass.getId(), grass,
                iron.getId(), iron
        );

        GlassentialRenderer.MimicSelection known = GlassentialRenderer.selectMimicState(
                grass.getId(), registered::get
        );
        assertEquals(GlassentialRenderer.MimicOutcome.RENDERED, known.outcome());
        assertEquals(grass, known.state());

        GlassentialRenderer.MimicSelection unknown = GlassentialRenderer.selectMimicState(
                Key.parse("example:not_registered"), registered::get
        );
        assertEquals(GlassentialRenderer.MimicOutcome.RENDERED, unknown.outcome());
        assertEquals(iron, unknown.state());

        GlassentialRenderer.MimicSelection recursive =
                GlassentialRenderer.selectMimicState(null, registered::get);
        assertEquals(GlassentialRenderer.MimicOutcome.RECURSIVE, recursive.outcome());
        assertNull(recursive.state());

        GlassentialRenderer.MimicSelection failed = GlassentialRenderer.selectMimicState(
                grass.getId(), key -> null
        );
        assertEquals(GlassentialRenderer.MimicOutcome.FAILED, failed.outcome());
        assertNull(failed.state());
    }

    private static BlockState state(String id, Map<String, String> properties) {
        return new BlockState(Key.parse(id), properties);
    }
}
