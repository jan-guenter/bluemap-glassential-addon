/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdapterBoundaryTest {

    @Test
    void usesSharedAdapterHelpersWithoutLocalCopies() {
        assertInstanceOf(ResourceExtensionType.class, BlueMap523Adapter.extensionType());
        assertEquals(
                Key.parse("bluemap_glassential:exact_profile"),
                BlueMap523Adapter.extensionType().getKey()
        );
        assertInstanceOf(
                GlassentialResourceExtension.class,
                BlueMap523Adapter.extensionType().create(null)
        );
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.glassential.adapter.bluemap523."
                        + "AdapterCompatibility"
        ));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.glassential.adapter.bluemap523."
                        + "GlassentialResourceExtensionType"
        ));
    }
}
