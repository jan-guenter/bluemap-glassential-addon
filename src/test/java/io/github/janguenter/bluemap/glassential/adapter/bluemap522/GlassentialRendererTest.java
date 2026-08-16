/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.math.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlassentialRendererTest {

    @Test
    void multipartColorsUseBlueMapsPremultipliedSumAndMaximumOpacity() {
        Color combined = new Color().set(0F, 0F, 0F, 0F, true);
        float[] opacity = {0F};
        GlassentialRenderer.addVariantColor(
                combined, opacity, new Color().set(1F, 0F, 0F, 0.25F, false)
        );
        GlassentialRenderer.addVariantColor(
                combined, opacity, new Color().set(0F, 0F, 1F, 0.5F, false)
        );
        combined.flatten().straight();
        combined.a = opacity[0];

        assertEquals(1F / 3F, combined.r, 0.000001F);
        assertEquals(0F, combined.g, 0F);
        assertEquals(2F / 3F, combined.b, 0.000001F);
        assertEquals(0.5F, combined.a, 0F);
        assertFalse(combined.premultiplied);
    }
}
