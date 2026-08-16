/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import java.util.Objects;
import java.util.Set;

/** Hash-locked blockstate and selected-model metadata for one exact routed block. */
public record GlassentialDefinition(
        String blockId,
        ShapeFamily shape,
        StateSchema stateSchema,
        ModelMode modelMode,
        int legalStates,
        String blockstateSha256,
        String directModelsSha256
) {

    private static final Set<String> VANILLA_OVERRIDES = Set.of(
            "minecraft:black_stained_glass",
            "minecraft:blue_stained_glass",
            "minecraft:brown_stained_glass",
            "minecraft:cyan_stained_glass",
            "minecraft:glass",
            "minecraft:gray_stained_glass",
            "minecraft:green_stained_glass",
            "minecraft:light_blue_stained_glass",
            "minecraft:light_gray_stained_glass",
            "minecraft:lime_stained_glass",
            "minecraft:magenta_stained_glass",
            "minecraft:orange_stained_glass",
            "minecraft:pink_stained_glass",
            "minecraft:purple_stained_glass",
            "minecraft:red_stained_glass",
            "minecraft:tinted_glass",
            "minecraft:white_stained_glass",
            "minecraft:yellow_stained_glass"
    );

    public GlassentialDefinition {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(stateSchema, "stateSchema");
        Objects.requireNonNull(modelMode, "modelMode");
        Objects.requireNonNull(blockstateSha256, "blockstateSha256");
        Objects.requireNonNull(directModelsSha256, "directModelsSha256");
        if (!(blockId.startsWith("glassential:") || VANILLA_OVERRIDES.contains(blockId))
                || legalStates != stateSchema.legalStates()
                || !compatible(shape, stateSchema, modelMode)
                || !blockstateSha256.matches("[0-9a-f]{64}")
                || !directModelsSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("malformed Glassential rendering definition");
        }
    }

    private static boolean compatible(
            ShapeFamily shape,
            StateSchema stateSchema,
            ModelMode modelMode
    ) {
        return switch (shape) {
            case FULL -> (stateSchema == StateSchema.PROPERTYLESS
                    || stateSchema == StateSchema.COLORABLE)
                    && modelMode == ModelMode.FUSION;
            case PANE -> (stateSchema == StateSchema.PANE
                    || stateSchema == StateSchema.COLORABLE_PANE)
                    && modelMode == ModelMode.FUSION_TEXTURED_REGULAR;
            case SLAB -> stateSchema == StateSchema.SLAB && modelMode == ModelMode.FUSION;
            case ONE_WAY -> stateSchema == StateSchema.ONE_WAY
                    && modelMode == ModelMode.ONE_WAY;
        };
    }
}
