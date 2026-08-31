/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Exact retained fields from Glassential's shared colorable block entity. */
public final class GlassentialColorBlockEntityData extends MCABlockEntity {

    @NBTName("Color")
    private Object color;

    @NBTName("EmitLight")
    private Object emitLight;

    /** Defensive value only; the route requires a persisted integer Color. */
    int rgb() {
        return color instanceof Integer integer
                ? integer & 0x00ffffff : 0x00ffffff;
    }

    boolean hasColor() {
        return color instanceof Integer;
    }

    boolean hasValidColorEncoding() {
        return color instanceof Integer;
    }

    boolean lightMatches(BlockState state) {
        if (!(emitLight instanceof Byte byteValue)
                || byteValue != 0 && byteValue != 1) {
            return false;
        }
        return (byteValue == 1) == "true".equals(state.getProperties().get("lit"));
    }
}
