/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

/** Rendering program family selected for one exact routed block. */
public enum ModelMode {
    FUSION("fusion"),
    FUSION_TEXTURED_REGULAR("fusion_textured_regular"),
    ONE_WAY("one_way");

    private final String wireName;

    ModelMode(String wireName) {
        this.wireName = wireName;
    }

    public static ModelMode parse(String value) {
        for (ModelMode mode : values()) {
            if (mode.wireName.equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("unknown Glassential model mode");
    }
}
