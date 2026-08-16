/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

/** Exact legal-state schemas in the bounded Glassential route. */
public enum StateSchema {
    PROPERTYLESS("propertyless", 1),
    PANE("pane", 32),
    COLORABLE("colorable", 2),
    COLORABLE_PANE("colorable_pane", 64),
    SLAB("slab", 6),
    ONE_WAY("one_way", 6);

    private final String wireName;
    private final int legalStates;

    StateSchema(String wireName, int legalStates) {
        this.wireName = wireName;
        this.legalStates = legalStates;
    }

    public int legalStates() {
        return legalStates;
    }

    public static StateSchema parse(String value) {
        for (StateSchema schema : values()) {
            if (schema.wireName.equals(value)) {
                return schema;
            }
        }
        throw new IllegalArgumentException("unknown Glassential state schema");
    }
}
