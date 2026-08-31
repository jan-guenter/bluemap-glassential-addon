/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.glassential.activation.GlassentialRuntime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** BlueMap 5.23 feature-backport ABI boundary. */
public final class BlueMap523Adapter {

    private static final GlassentialRuntime RUNTIME = GlassentialRuntime.INSTANCE;
    private static final Key EXTENSION_KEY =
            Key.parse("bluemap_glassential:exact_profile");
    static final de.bluecolored.bluemap.core.util.Key RENDERER_KEY =
            de.bluecolored.bluemap.core.util.Key.parse("bluemap_glassential:fusion_model");
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            RENDERER_KEY,
            (pack, gallery, settings) -> new GlassentialRenderer(pack, gallery, settings, RUNTIME)
    );
    private static final ResourcePack.Extension<GlassentialResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    EXTENSION_KEY,
                    pack -> new GlassentialResourceExtension(pack, RUNTIME)
            );
    static final Key COLOR_BLOCK_ENTITY_KEY = Key.parse("glassential:colorable_glass");
    static final Key ONE_WAY_BLOCK_ENTITY_KEY = Key.parse("glassential:one_way_glass");
    private static final BlockEntityType COLOR_BLOCK_ENTITY = new BlockEntityType.Impl(
            COLOR_BLOCK_ENTITY_KEY, GlassentialColorBlockEntityData.class
    );
    private static final BlockEntityType ONE_WAY_BLOCK_ENTITY = new BlockEntityType.Impl(
            ONE_WAY_BLOCK_ENTITY_KEY, GlassentialOneWayBlockEntityData.class
    );

    private BlueMap523Adapter() {
    }

    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.canRegister(BlockEntityType.REGISTRY, COLOR_BLOCK_ENTITY)
                || !RegistryGuard.canRegister(BlockEntityType.REGISTRY, ONE_WAY_BLOCK_ENTITY)) {
            RUNTIME.disable("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.register(BlockEntityType.REGISTRY, COLOR_BLOCK_ENTITY)
                || !RegistryGuard.register(BlockEntityType.REGISTRY, ONE_WAY_BLOCK_ENTITY)) {
            RUNTIME.disable("registry-collision");
            return false;
        }
        return true;
    }

    /**
     * Exercises BlueMap's real shared BlockEntity decoder after registration.
     * BlueNBT snapshots resolver subtypes on first use, so this also detects an
     * add-on that was initialized too late in the process lifecycle.
     */
    static boolean verifyBlockEntityRetention() {
        try {
            BlockEntity color = decode(colorProbe(0x12ab34, false));
            BlockEntity oneWay = decode(probe(
                    ONE_WAY_BLOCK_ENTITY_KEY.getFormatted(), "Mimic", "minecraft:stone"
            ));
            return color instanceof GlassentialColorBlockEntityData colorData
                    && colorData.hasColor() && colorData.rgb() == 0x12ab34
                    && colorData.lightMatches(new de.bluecolored.bluemap.core.world.BlockState(
                            COLOR_BLOCK_ENTITY_KEY, java.util.Map.of("lit", "false")
                    ))
                    && oneWay instanceof GlassentialOneWayBlockEntityData oneWayData
                    && Key.parse("minecraft:stone").equals(oneWayData.mimicOrIron());
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static BlockEntity decode(byte[] nbt) throws IOException {
        return MCAUtil.BLUENBT.read(new ByteArrayInputStream(nbt), BlockEntity.class);
    }

    static byte[] probe(String id, String field, Object value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(output)) {
            writer.beginCompound();
            writer.name("id").value(id);
            writer.name("x").value(0);
            writer.name("y").value(0);
            writer.name("z").value(0);
            if (value instanceof Integer integer) {
                writer.name(field).value(integer);
            } else if (value instanceof Byte byteValue) {
                writer.name(field).value(byteValue);
            } else if (value instanceof String string) {
                writer.name(field).value(string);
            } else if (value != null) {
                throw new IllegalArgumentException("unsupported block-entity probe value");
            }
            writer.endCompound();
        }
        return output.toByteArray();
    }

    static byte[] colorProbe(int color, boolean emitLight) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(output)) {
            writer.beginCompound();
            writer.name("id").value(COLOR_BLOCK_ENTITY_KEY.getFormatted());
            writer.name("x").value(0);
            writer.name("y").value(0);
            writer.name("z").value(0);
            writer.name("Color").value(color);
            writer.name("EmitLight").value((byte) (emitLight ? 1 : 0));
            writer.endCompound();
        }
        return output.toByteArray();
    }

    static BlockRendererType renderer() {
        return RENDERER;
    }

    static GlassentialResourceExtension extension(ResourcePack resourcePack) {
        return resourcePack.getExtension(EXTENSION);
    }

    static ResourcePack.Extension<GlassentialResourceExtension> extensionType() {
        return EXTENSION;
    }
}
