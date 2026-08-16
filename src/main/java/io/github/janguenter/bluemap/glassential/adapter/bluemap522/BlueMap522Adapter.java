/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.glassential.activation.GlassentialRuntime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** BlueMap 5.22 internal ABI boundary. */
public final class BlueMap522Adapter {

    private static final GlassentialRuntime RUNTIME = GlassentialRuntime.INSTANCE;
    static final de.bluecolored.bluemap.core.util.Key RENDERER_KEY =
            de.bluecolored.bluemap.core.util.Key.parse("bluemap_glassential:fusion_model");
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            RENDERER_KEY,
            (pack, gallery, settings) -> new GlassentialRenderer(pack, gallery, settings, RUNTIME)
    );
    private static final ResourcePack.Extension<GlassentialResourceExtension> EXTENSION =
            new GlassentialResourceExtensionType(RUNTIME);
    static final Key COLOR_BLOCK_ENTITY_KEY = Key.parse("glassential:colorable_glass");
    static final Key ONE_WAY_BLOCK_ENTITY_KEY = Key.parse("glassential:one_way_glass");
    private static final BlockEntityType COLOR_BLOCK_ENTITY = new BlockEntityType.Impl(
            COLOR_BLOCK_ENTITY_KEY, GlassentialColorBlockEntityData.class
    );
    private static final BlockEntityType ONE_WAY_BLOCK_ENTITY = new BlockEntityType.Impl(
            ONE_WAY_BLOCK_ENTITY_KEY, GlassentialOneWayBlockEntityData.class
    );

    private BlueMap522Adapter() {
    }

    public static synchronized boolean install() {
        if (!canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !canRegister(BlockEntityType.REGISTRY, COLOR_BLOCK_ENTITY)
                || !canRegister(BlockEntityType.REGISTRY, ONE_WAY_BLOCK_ENTITY)) {
            RUNTIME.disable("registry-collision");
            return false;
        }
        if (!register(BlockRendererType.REGISTRY, RENDERER)
                || !register(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !register(BlockEntityType.REGISTRY, COLOR_BLOCK_ENTITY)
                || !register(BlockEntityType.REGISTRY, ONE_WAY_BLOCK_ENTITY)) {
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

    static boolean isExpectedDispatch(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static GlassentialResourceExtension extension(ResourcePack resourcePack) {
        return resourcePack.getExtension(EXTENSION);
    }

    private static <T extends Keyed> boolean canRegister(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        return existing == null || existing == candidate;
    }

    private static <T extends Keyed> boolean register(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        if (existing == null) {
            registry.register(candidate);
            existing = registry.get(candidate.getKey());
        }
        return existing == candidate;
    }
}
