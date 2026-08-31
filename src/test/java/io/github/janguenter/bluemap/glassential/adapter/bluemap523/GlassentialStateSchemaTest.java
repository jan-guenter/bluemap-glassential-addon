/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Tristate;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.glassential.profile.GlassentialDefinition;
import io.github.janguenter.bluemap.glassential.profile.ModelMode;
import io.github.janguenter.bluemap.glassential.profile.ShapeFamily;
import io.github.janguenter.bluemap.glassential.profile.StateSchema;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlassentialStateSchemaTest {

    private static final String PANE_ID = "glassential:glass_light_pane";
    private static final GlassentialDefinition PANE = definition(
            PANE_ID, ShapeFamily.PANE, StateSchema.PANE,
            ModelMode.FUSION_TEXTURED_REGULAR
    );
    private static final GlassentialDefinition FULL = definition(
            "glassential:glass_light", ShapeFamily.FULL,
            StateSchema.PROPERTYLESS, ModelMode.FUSION
    );

    @Test
    void acceptsEveryExactPaneAndColorablePaneCombination() {
        for (int mask = 0; mask < 32; mask++) {
            assertTrue(GlassentialStateSchema.accepts(pane(mask), PANE));
        }
        GlassentialDefinition colorablePane = definition(
                "glassential:colorable_glass_pane", ShapeFamily.PANE,
                StateSchema.COLORABLE_PANE, ModelMode.FUSION_TEXTURED_REGULAR
        );
        for (int mask = 0; mask < 64; mask++) {
            Map<String, String> properties = new HashMap<>(pane(mask).getProperties());
            properties.put("lit", bit(mask, 5));
            assertTrue(GlassentialStateSchema.accepts(state(
                    colorablePane.blockId(), properties
            ), colorablePane));
        }
    }

    @Test
    void acceptsOnlyTheSixExactSchemas() {
        GlassentialDefinition colorable = definition(
                "glassential:colorable_glass", ShapeFamily.FULL,
                StateSchema.COLORABLE, ModelMode.FUSION
        );
        GlassentialDefinition slab = definition(
                "glassential:glass_slab", ShapeFamily.SLAB,
                StateSchema.SLAB, ModelMode.FUSION
        );
        GlassentialDefinition oneWay = definition(
                "glassential:one_way_glass", ShapeFamily.ONE_WAY,
                StateSchema.ONE_WAY, ModelMode.ONE_WAY
        );
        assertTrue(GlassentialStateSchema.accepts(
                state(colorable.blockId(), Map.of("lit", "false")), colorable
        ));
        assertTrue(GlassentialStateSchema.accepts(
                state(colorable.blockId(), Map.of("lit", "true")), colorable
        ));
        for (String type : new String[]{"bottom", "top", "double"}) {
            for (String waterlogged : new String[]{"false", "true"}) {
                assertTrue(GlassentialStateSchema.accepts(state(
                        slab.blockId(), Map.of(
                                "type", type, "waterlogged", waterlogged
                        )
                ), slab));
            }
        }
        for (String face : new String[]{
                "down", "up", "north", "south", "west", "east"
        }) {
            assertTrue(GlassentialStateSchema.accepts(state(
                    oneWay.blockId(), Map.of("opaque_face", face)
            ), oneWay));
        }
        assertFalse(GlassentialStateSchema.accepts(state(
                slab.blockId(), Map.of("type", "side", "waterlogged", "false")
        ), slab));
        assertFalse(GlassentialStateSchema.accepts(state(
                oneWay.blockId(), Map.of("opaque_face", "inside")
        ), oneWay));
    }

    @Test
    void rejectsMissingExtraInvalidAndWrongIdProperties() {
        Map<String, String> valid = new HashMap<>(pane(0).getProperties());
        valid.remove("north");
        assertFalse(GlassentialStateSchema.accepts(state(PANE_ID, valid), PANE));

        valid = new HashMap<>(pane(0).getProperties());
        valid.put("legacy", "false");
        assertFalse(GlassentialStateSchema.accepts(state(PANE_ID, valid), PANE));

        valid = new HashMap<>(pane(0).getProperties());
        valid.put("east", "maybe");
        assertFalse(GlassentialStateSchema.accepts(state(PANE_ID, valid), PANE));

        assertFalse(GlassentialStateSchema.accepts(
                state("glassential:glass_ghostly_pane", pane(0).getProperties()),
                PANE
        ));
    }

    @Test
    void propertylessCubesAcceptOnlyTheirExactState() {
        assertTrue(GlassentialStateSchema.accepts(
                state(FULL.blockId(), Map.of()), FULL
        ));
        assertFalse(GlassentialStateSchema.accepts(
                state(FULL.blockId(), Map.of("legacy", "true")), FULL
        ));
        assertFalse(GlassentialStateSchema.accepts(
                state("glassential:glass_ghostly", Map.of()), FULL
        ));
    }

    @Test
    void rejectedStatesReceiveExplicitSafeProperties() {
        assertSafeProperties(state(PANE_ID, Map.of()), PANE);
        assertSafeProperties(
                state(FULL.blockId(), Map.of("legacy", "true")), FULL
        );
    }

    private static void assertSafeProperties(
            BlockState state,
            GlassentialDefinition definition
    ) {
        BlockProperties.Builder builder = BlockProperties.builder();
        GlassentialResourceExtension.applyProperties(state, definition, builder);

        assertEquals(Tristate.FALSE, builder.isCulling());
        assertEquals(Tristate.FALSE, builder.isOccluding());
        assertEquals(Tristate.FALSE, builder.isCullingIdentical());
    }

    private static BlockState pane(int mask) {
        return state(PANE_ID, Map.of(
                "north", bit(mask, 0),
                "east", bit(mask, 1),
                "south", bit(mask, 2),
                "west", bit(mask, 3),
                "waterlogged", bit(mask, 4)
        ));
    }

    private static String bit(int mask, int bit) {
        return (mask & 1 << bit) == 0 ? "false" : "true";
    }

    private static BlockState state(String id, Map<String, String> properties) {
        return new BlockState(Key.parse(id), properties);
    }

    private static GlassentialDefinition definition(
            String id,
            ShapeFamily shape,
            StateSchema schema,
            ModelMode mode
    ) {
        return new GlassentialDefinition(
                id, shape, schema, mode, schema.legalStates(),
                "0".repeat(64), "1".repeat(64)
        );
    }
}
