/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.glassential.activation.GlassentialRuntime;
import io.github.janguenter.bluemap.glassential.profile.ExactModArtifactDetector;
import io.github.janguenter.bluemap.glassential.profile.ProfileDisablement;
import io.github.janguenter.bluemap.glassential.profile.Glassential345Fusion1312Profile;
import io.github.janguenter.bluemap.glassential.profile.GlassentialDefinition;
import io.github.janguenter.bluemap.glassential.profile.TextureCatalog;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Exact artifact/schema activation, tile cropping, and allowlist routing. */
final class GlassentialResourceExtension implements ResourcePackExtension {

    static final Key SYNTHETIC = Key.parse("bluemap_glassential:fusion_model");

    private final ResourcePack resourcePack;
    private final GlassentialRuntime runtime;
    private Map<TileKey, Key> tileKeys = Map.of();
    private volatile MinecraftDefaultStateResolver defaultStates;

    GlassentialResourceExtension(ResourcePack resourcePack, GlassentialRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws IOException, InterruptedException {
        defaultStates = null;
        try {
            loadVerifiedResources(roots);
        } catch (IOException | RuntimeException exception) {
            defaultStates = null;
            runtime.inactive("active-resource-read-failed");
        }
    }

    private void loadVerifiedResources(Iterable<Path> roots)
            throws IOException, InterruptedException {
        if (ProfileDisablement.current().isDisabled(
                Glassential345Fusion1312Profile.PROFILE_ID
        )) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactModArtifactDetector.matchesRequiredPair(roots)) {
            runtime.inactive("exact-artifact-pair-missing");
            return;
        }
        if (!BlueMap522Adapter.verifyBlockEntityRetention()) {
            runtime.disable("block-entity-codec-unavailable");
            return;
        }
        MinecraftDefaultStateResolver candidateDefaults =
                MinecraftDefaultStateResolver.createVerified();
        if (candidateDefaults == null) {
            runtime.disable("minecraft-default-state-registry-unavailable");
            return;
        }
        ActiveResourceSchemaValidator.Result schema = ActiveResourceSchemaValidator.validate(
                resourcePack,
                roots,
                Glassential345Fusion1312Profile.RESOURCES,
                Glassential345Fusion1312Profile.TEXTURES
        );
        if (!schema.valid()) {
            runtime.inactive(schema.reason());
            return;
        }
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState dispatch =
                resourcePack.getBlockStates().get(SYNTHETIC);
        if (!validDispatch(dispatch)) {
            runtime.inactive("synthetic-dispatch-invalid");
            return;
        }
        defaultStates = candidateDefaults;
        runtime.activate(schema.catalog());
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        if (!runtime.route().isActive()) {
            return Set.of();
        }
        Set<Key> used = new LinkedHashSet<>(
                Glassential345Fusion1312Profile.TEXTURES.keys()
        );
        used.addAll(plannedTiles().values());
        return Set.copyOf(used);
    }

    @Override
    public void bake() {
        if (!runtime.route().isActive()) {
            return;
        }
        try {
            Map<TileKey, Key> planned = plannedTiles();
            for (Key output : planned.values()) {
                if (resourcePack.getTextures().get(output) != null) {
                    runtime.inactive("synthetic-texture-collision");
                    return;
                }
            }
            Map<Key, Texture> generated = cropTiles(planned);
            if (generated.size() != planned.size()) {
                runtime.inactive("required-texture-invalid");
                return;
            }
            generated.forEach(resourcePack.getTextures()::put);
            tileKeys = Map.copyOf(planned);
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("required-texture-invalid");
        }
    }

    @Override
    public Key getBlockStateKey(Key key) {
        return runtime.route().isActive()
                && Glassential345Fusion1312Profile.ROUTED_BLOCKS.contains(key.getFormatted())
                ? SYNTHETIC : key;
    }

    @Override
    public void getBlockProperties(BlockState state, BlockProperties.Builder builder) {
        if (!runtime.route().isActive()) {
            return;
        }
        GlassentialDefinition definition = Glassential345Fusion1312Profile.DEFINITIONS.get(
                state.getId().getFormatted()
        );
        if (definition == null) {
            return;
        }
        applyProperties(state, definition, builder);
    }

    static void applyProperties(
            BlockState state,
            GlassentialDefinition definition,
            BlockProperties.Builder builder
    ) {
        if (!GlassentialStateSchema.accepts(state, definition)) {
            builder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
            return;
        }
        switch (definition.shape()) {
            case FULL -> builder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(true);
            case PANE -> builder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
            case SLAB, ONE_WAY -> builder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        }
    }

    Key tile(Key source, int index) {
        return tileKeys.get(new TileKey(source, index));
    }

    BlockState defaultState(Key key) {
        MinecraftDefaultStateResolver resolver = defaultStates;
        if (resolver == null) {
            throw new IllegalStateException("Minecraft default-state resolver is inactive");
        }
        return resolver.resolveRegistered(key);
    }

    private Map<TileKey, Key> plannedTiles() {
        Map<TileKey, Key> planned = new LinkedHashMap<>();
        Set<Key> outputs = new LinkedHashSet<>();
        for (Key source : Glassential345Fusion1312Profile.TEXTURES.keys()) {
            TextureCatalog.Entry entry = Glassential345Fusion1312Profile.TEXTURES.get(source);
            int count = entry.layout().columns() * entry.layout().rows();
            for (int index = 0; index < count; index++) {
                Key output = Key.parse("bluemap_glassential:tiles/"
                        + source.getNamespace() + "/" + source.getValue() + "/" + index);
                if (!outputs.add(output)) {
                    throw new IllegalArgumentException("synthetic texture key collision");
                }
                planned.put(new TileKey(source, index), output);
            }
        }
        return planned;
    }

    private Map<Key, Texture> cropTiles(Map<TileKey, Key> planned) throws IOException {
        Map<Key, Texture> generated = new LinkedHashMap<>();
        for (Map.Entry<TileKey, Key> request : planned.entrySet()) {
            TileKey tile = request.getKey();
            Texture sourceTexture = resourcePack.getTextures().get(tile.source());
            TextureCatalog.Entry entry = Glassential345Fusion1312Profile.TEXTURES.get(tile.source());
            if (sourceTexture == null || entry == null) {
                throw new IOException("required source sheet is missing");
            }
            BufferedImage sheet = sourceTexture.getTextureImage();
            if (!validBakedSheetDimensions(sheet, entry)) {
                throw new IOException("baked source sheet dimensions changed");
            }
            int tileWidth = entry.width() / entry.layout().columns();
            int tileHeight = entry.height() / entry.layout().physicalRows();
            int x = tile.index() % entry.layout().columns() * tileWidth;
            int y = tile.index() / entry.layout().columns() * tileHeight;
            if (tile.index() >= entry.layout().columns() * entry.layout().rows()
                    || x + tileWidth > sheet.getWidth()
                    || y + tileHeight > sheet.getHeight()) {
                throw new IOException("sheet tile leaves active logical crop");
            }
            BufferedImage copy = new BufferedImage(
                    tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D graphics = copy.createGraphics();
            try {
                graphics.drawImage(sheet, -x, -y, null);
            } finally {
                graphics.dispose();
            }
            generated.put(request.getValue(), Texture.from(request.getValue(), copy));
        }
        return generated;
    }

    static boolean validBakedSheetDimensions(
            BufferedImage sheet,
            TextureCatalog.Entry entry
    ) {
        return sheet.getWidth() == entry.width() && sheet.getHeight() == entry.height();
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        if (state == null || state.getMultipart() != null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet set = variants.getDefaultVariant();
        if (set.getVariants().length != 1) {
            return false;
        }
        Variant variant = set.getVariants()[0];
        return BlueMap522Adapter.isExpectedDispatch(variant);
    }

    private record TileKey(Key source, int index) {
    }
}
