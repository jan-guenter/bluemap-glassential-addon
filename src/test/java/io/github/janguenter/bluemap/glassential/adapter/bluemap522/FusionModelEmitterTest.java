/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.glassential.profile.ShapeFamily;
import io.github.janguenter.bluemap.glassential.profile.TextureLayout;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FusionModelEmitterTest {

    @Test
    void tintedTranslucentMapSamplesRemainPremultipliedForAggregation() {
        Color sample = new Color().set(0.2F, 0.1F, 0.05F, 0.5F, true);
        Color tint = new Color().set(0.25F, 0.5F, 1F, 1F, false);

        FusionModelEmitter.tintForPremultipliedAccumulation(sample, tint);

        assertTrue(sample.premultiplied);
        assertEquals(0.05F, sample.r);
        assertEquals(0.05F, sample.g);
        assertEquals(0.05F, sample.b);
        assertEquals(0.5F, sample.a);
        Color accumulator = new Color().set(0F, 0F, 0F, 0F, true);
        assertDoesNotThrow(() -> accumulator.add(sample));
    }

    @Test
    void piecedClippingDropsZeroAreaSlabSeams() {
        assertEquals(4, FusionModelEmitter.piecedPartCount(0F, 0F, 1F, 1F));
        assertEquals(2, FusionModelEmitter.piecedPartCount(0F, 0.5F, 1F, 1F));
        assertEquals(1, FusionModelEmitter.piecedPartCount(0F, 0.5F, 0.5F, 1F));
        assertEquals(0, FusionModelEmitter.piecedPartCount(0F, 0.5F, 1F, 0.5F));
    }

    @Test
    void plainEdgeTexturesDoNotNeedConnectionFrames() {
        assertFalse(FusionModelEmitter.requiresConnectionFrame(TextureLayout.PLAIN));
        assertTrue(FusionModelEmitter.requiresConnectionFrame(TextureLayout.FULL));
        assertTrue(FusionModelEmitter.requiresConnectionFrame(TextureLayout.SIMPLE));
        assertTrue(FusionModelEmitter.requiresConnectionFrame(TextureLayout.PIECED));
    }

    @Test
    void oneWayFaceFiltersUseOnlyDirectionalCullfaceBuckets() {
        for (Direction opaque : Direction.values()) {
            int opaqueCount = 0;
            int transparentCount = 0;
            for (Direction cullface : Direction.values()) {
                if (FusionModelEmitter.includesCullfaceBucket(
                        cullface, opaque, true
                )) {
                    opaqueCount++;
                }
                if (FusionModelEmitter.includesCullfaceBucket(
                        cullface, opaque, false
                )) {
                    transparentCount++;
                }
                assertTrue(FusionModelEmitter.includesCullfaceBucket(
                        cullface, null, true
                ));
            }
            assertEquals(1, opaqueCount);
            assertEquals(5, transparentCount);
            assertFalse(FusionModelEmitter.includesCullfaceBucket(
                    null, opaque, true
            ));
            assertFalse(FusionModelEmitter.includesCullfaceBucket(
                    null, opaque, false
            ));
        }
    }

    @Test
    void fullCubesCullSameIdButNeverOtherIds() {
        BlockState clear = state("glassential:glass_light", Map.of());
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.FULL, Direction.NORTH, clear, clear
        ));
        assertFalse(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.FULL,
                Direction.NORTH,
                clear,
                state("glassential:glass_ghostly", Map.of())
        ));
        BlockState vanilla = state("minecraft:glass", Map.of());
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.FULL, Direction.NORTH, vanilla, vanilla
        ));
    }

    @Test
    void panesCullOnlyReciprocalHorizontalArmsAndIgnoreWaterlogging() {
        BlockState own = pane("true", "false", "false", "false", "false");
        BlockState reciprocalWet = pane("false", "false", "true", "false", "true");
        BlockState nonreciprocal = pane("false", "false", "false", "false", "false");
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.PANE, Direction.NORTH, own, reciprocalWet
        ));
        assertFalse(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.PANE, Direction.NORTH, own, nonreciprocal
        ));
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.PANE, Direction.UP, own, reciprocalWet
        ));
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.PANE, Direction.DOWN, own, reciprocalWet
        ));
        assertFalse(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.PANE,
                Direction.UP,
                own,
                state("glassential:glass_ghostly_pane", reciprocalWet.getProperties())
        ));
    }

    @Test
    void oneWayGlassCullsSameIdAcrossDifferentOpaqueFaces() {
        BlockState north = oneWay("north");
        BlockState south = oneWay("south");
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.ONE_WAY, Direction.EAST, north, south
        ));
        assertFalse(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.ONE_WAY,
                Direction.EAST,
                north,
                state("glassential:tinted_one_way_glass", Map.of(
                        "opaque_face", "south"
                ))
        ));
    }

    @Test
    void slabCullingMatchesInstalledDirectionalContract() {
        assertFalse(FusionModelEmitter.slabFaceCulls(
                Direction.UP, "bottom", "bottom"
        ));
        assertTrue(FusionModelEmitter.slabFaceCulls(
                Direction.UP, "top", "bottom"
        ));
        assertFalse(FusionModelEmitter.slabFaceCulls(
                Direction.UP, "top", "top"
        ));
        assertTrue(FusionModelEmitter.slabFaceCulls(
                Direction.DOWN, "bottom", "top"
        ));
        assertFalse(FusionModelEmitter.slabFaceCulls(
                Direction.DOWN, "top", "top"
        ));
        assertTrue(FusionModelEmitter.slabFaceCulls(
                Direction.NORTH, "bottom", "bottom"
        ));
        assertTrue(FusionModelEmitter.slabFaceCulls(
                Direction.NORTH, "top", "double"
        ));
        assertFalse(FusionModelEmitter.slabFaceCulls(
                Direction.NORTH, "top", "bottom"
        ));

        BlockState own = slab("bottom", "true");
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.SLAB,
                Direction.EAST,
                own,
                state("minecraft:glass", Map.of())
        ));
        assertTrue(FusionModelEmitter.sameRoutedFaceCulls(
                ShapeFamily.SLAB, Direction.NORTH, own, slab("bottom", "false")
        ));
    }

    private static BlockState pane(
            String north,
            String east,
            String south,
            String west,
            String waterlogged
    ) {
        return state("glassential:glass_light_pane", Map.of(
                "north", north,
                "east", east,
                "south", south,
                "west", west,
                "waterlogged", waterlogged
        ));
    }

    private static BlockState slab(String type, String waterlogged) {
        return state("glassential:glass_slab", Map.of(
                "type", type,
                "waterlogged", waterlogged
        ));
    }

    private static BlockState oneWay(String opaqueFace) {
        return state("glassential:one_way_glass", Map.of(
                "opaque_face", opaqueFace
        ));
    }

    private static BlockState state(String id, Map<String, String> properties) {
        return new BlockState(Key.parse(id), properties);
    }
}
