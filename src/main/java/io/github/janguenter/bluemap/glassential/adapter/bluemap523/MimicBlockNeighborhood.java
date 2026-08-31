/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;

/** One-block default-state view used for one-way glass's opaque mimic face. */
final class MimicBlockNeighborhood extends BlockNeighborhood {

    private final BlockNeighborhood source;
    private final BlockState mimic;

    MimicBlockNeighborhood(BlockNeighborhood source, BlockState mimic) {
        super(
                source,
                source.getResourcePack(),
                source.getRenderSettings(),
                source.getDimensionType()
        );
        this.source = source;
        this.mimic = mimic;
        super.set(source.getX(), source.getY(), source.getZ());
    }

    @Override
    public BlockState getBlockState() {
        return mimic;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public BlockProperties getProperties() {
        return getResourcePack().getBlockProperties(mimic);
    }

    @Override
    public ExtendedBlock getNeighborBlock(int dx, int dy, int dz) {
        return dx == 0 && dy == 0 && dz == 0
                ? this : source.getNeighborBlock(dx, dy, dz);
    }
}
