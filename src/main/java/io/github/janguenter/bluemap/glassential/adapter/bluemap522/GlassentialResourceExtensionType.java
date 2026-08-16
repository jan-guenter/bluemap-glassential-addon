/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.glassential.activation.GlassentialRuntime;

/** Resource-pack extension factory registered before resource loading begins. */
final class GlassentialResourceExtensionType
        implements ResourcePack.Extension<GlassentialResourceExtension> {

    static final Key KEY = Key.parse("bluemap_glassential:exact_profile");

    private final GlassentialRuntime runtime;

    GlassentialResourceExtensionType(GlassentialRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public GlassentialResourceExtension create(ResourcePack pack) {
        return new GlassentialResourceExtension(pack, runtime);
    }
}
