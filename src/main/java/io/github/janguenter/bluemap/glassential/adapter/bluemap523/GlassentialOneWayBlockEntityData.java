/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;
import io.github.janguenter.bluemap.glassential.profile.Glassential345Fusion1312Profile;

/** Exact retained field from Glassential's shared one-way block entity. */
public final class GlassentialOneWayBlockEntityData extends MCABlockEntity {

    static final Key IRON_BLOCK = Key.minecraft("iron_block");

    @NBTName("Mimic")
    private Object mimic;

    /** Null means an explicit recursive one-way target and therefore base glass. */
    Key mimicOrIron() {
        if (!(mimic instanceof String value)
                || value.isBlank() || value.length() > 256
                || !validResourceLocation(value)) {
            return IRON_BLOCK;
        }
        try {
            Key key = Key.parse(value);
            return Glassential345Fusion1312Profile.ONE_WAY_BLOCK_IDS.contains(
                    key.getFormatted()
            ) ? null : key;
        } catch (IllegalArgumentException exception) {
            return IRON_BLOCK;
        }
    }

    private static boolean validResourceLocation(String value) {
        int colon = value.indexOf(':');
        if (colon != value.lastIndexOf(':')) {
            return false;
        }
        String namespace = colon < 0 ? "minecraft" : value.substring(0, colon);
        String path = colon < 0 ? value : value.substring(colon + 1);
        return namespace.matches("[a-z0-9_.-]+")
                && path.matches("[a-z0-9/._-]+");
    }
}
