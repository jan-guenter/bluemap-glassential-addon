/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlassentialBlockEntityDataTest {

    @BeforeAll
    static void installExactTypesBeforeDecoderUse() {
        assertTrue(BlueMap522Adapter.install());
        assertTrue(BlueMap522Adapter.verifyBlockEntityRetention());
    }

    @Test
    void retainsColorAndLightAndRejectsMissingPersistedColor() throws IOException {
        GlassentialColorBlockEntityData colored = assertInstanceOf(
                GlassentialColorBlockEntityData.class,
                BlueMap522Adapter.decode(BlueMap522Adapter.colorProbe(0x123456, true))
        );
        assertEquals(0x123456, colored.rgb());
        assertTrue(colored.lightMatches(colorState("true")));
        assertFalse(colored.lightMatches(colorState("false")));

        GlassentialColorBlockEntityData noColor = assertInstanceOf(
                GlassentialColorBlockEntityData.class,
                BlueMap522Adapter.decode(BlueMap522Adapter.probe(
                        BlueMap522Adapter.COLOR_BLOCK_ENTITY_KEY.getFormatted(),
                        "EmitLight", (byte) 0
                ))
        );
        assertEquals(0xffffff, noColor.rgb());
        assertTrue(noColor.lightMatches(colorState("false")));
        assertFalse(noColor.hasValidColorEncoding());
        assertFalse(GlassentialRenderer.validColorData(
                colorState("false"), noColor
        ));
    }

    @Test
    void malformedDynamicTypesFailClosedToGenericBlockEntity() throws IOException {
        BlockEntity badColor = BlueMap522Adapter.decode(BlueMap522Adapter.probe(
                BlueMap522Adapter.COLOR_BLOCK_ENTITY_KEY.getFormatted(),
                "EmitLight", "not-a-byte"
        ));
        assertFalse(GlassentialRenderer.validColorData(colorState("false"), badColor));

        BlockEntity badMimic = BlueMap522Adapter.decode(BlueMap522Adapter.probe(
                BlueMap522Adapter.ONE_WAY_BLOCK_ENTITY_KEY.getFormatted(),
                "Mimic", 42
        ));
        assertEquals(
                GlassentialOneWayBlockEntityData.IRON_BLOCK,
                GlassentialRenderer.mimicTarget(badMimic)
        );
    }

    @Test
    void resolvesMissingInvalidUnknownAndRecursiveMimics() throws IOException {
        GlassentialOneWayBlockEntityData missing = oneWay(null);
        GlassentialOneWayBlockEntityData invalid = oneWay("Bad Namespace:stone");
        GlassentialOneWayBlockEntityData unknown = oneWay("example:not_installed");
        GlassentialOneWayBlockEntityData recursive = oneWay(
                "glassential:one_way_glass"
        );

        assertEquals(GlassentialOneWayBlockEntityData.IRON_BLOCK, missing.mimicOrIron());
        assertEquals(GlassentialOneWayBlockEntityData.IRON_BLOCK, invalid.mimicOrIron());
        assertEquals(Key.parse("example:not_installed"), unknown.mimicOrIron());
        assertNull(recursive.mimicOrIron());
    }

    private static GlassentialOneWayBlockEntityData oneWay(String mimic)
            throws IOException {
        return assertInstanceOf(
                GlassentialOneWayBlockEntityData.class,
                BlueMap522Adapter.decode(BlueMap522Adapter.probe(
                        BlueMap522Adapter.ONE_WAY_BLOCK_ENTITY_KEY.getFormatted(),
                        "Mimic", mimic
                ))
        );
    }

    private static BlockState colorState(String lit) {
        return new BlockState(
                BlueMap522Adapter.COLOR_BLOCK_ENTITY_KEY,
                Map.of("lit", lit)
        );
    }
}
