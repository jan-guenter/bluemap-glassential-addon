/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.glassential.profile.GlassentialDefinition;

import java.util.Map;
import java.util.Set;

/** Exact persisted-state boundary for all 49 routed block IDs. */
final class GlassentialStateSchema {

    private static final Set<String> PANE_PROPERTIES = Set.of(
            "north", "east", "south", "west", "waterlogged"
    );
    private static final Set<String> COLORABLE_PANE_PROPERTIES = Set.of(
            "north", "east", "south", "west", "waterlogged", "lit"
    );
    private static final Set<String> SLAB_PROPERTIES = Set.of("type", "waterlogged");
    private static final Set<String> BOOLEAN_VALUES = Set.of("false", "true");
    private static final Set<String> SLAB_TYPES = Set.of("bottom", "top", "double");
    private static final Set<String> FACES = Set.of(
            "down", "up", "north", "south", "west", "east"
    );

    private GlassentialStateSchema() {
    }

    static boolean accepts(BlockState state, GlassentialDefinition definition) {
        if (state == null || definition == null
                || !definition.blockId().equals(state.getId().getFormatted())) {
            return false;
        }
        Map<String, String> properties = state.getProperties();
        return switch (definition.stateSchema()) {
            case PROPERTYLESS -> properties.isEmpty();
            case PANE -> hasBooleanProperties(properties, PANE_PROPERTIES);
            case COLORABLE -> properties.keySet().equals(Set.of("lit"))
                    && BOOLEAN_VALUES.contains(properties.get("lit"));
            case COLORABLE_PANE -> hasBooleanProperties(
                    properties, COLORABLE_PANE_PROPERTIES
            );
            case SLAB -> properties.keySet().equals(SLAB_PROPERTIES)
                    && SLAB_TYPES.contains(properties.get("type"))
                    && BOOLEAN_VALUES.contains(properties.get("waterlogged"));
            case ONE_WAY -> properties.keySet().equals(Set.of("opaque_face"))
                    && FACES.contains(properties.get("opaque_face"));
        };
    }

    private static boolean hasBooleanProperties(
            Map<String, String> properties,
            Set<String> expected
    ) {
        return properties.keySet().equals(expected)
                && properties.values().stream().allMatch(BOOLEAN_VALUES::contains);
    }
}
