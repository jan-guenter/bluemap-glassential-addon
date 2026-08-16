/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import java.util.Locale;

/** Stable geometry families in the exact routed Glassential roster. */
public enum ShapeFamily {
    FULL("full"),
    PANE("pane"),
    SLAB("slab"),
    ONE_WAY("one_way");

    private final String wireName;

    ShapeFamily(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static ShapeFamily parse(String value) {
        for (ShapeFamily family : values()) {
            if (family.wireName.equals(value.toLowerCase(Locale.ROOT))) {
                return family;
            }
        }
        throw new IllegalArgumentException("unknown Glassential shape family");
    }
}
