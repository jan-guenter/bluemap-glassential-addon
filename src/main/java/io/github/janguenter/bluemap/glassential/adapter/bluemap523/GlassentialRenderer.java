/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.glassential.activation.GlassentialRuntime;
import io.github.janguenter.bluemap.glassential.profile.Glassential345Fusion1312Profile;
import io.github.janguenter.bluemap.glassential.profile.GlassentialDefinition;
import io.github.janguenter.bluemap.glassential.profile.ModelMode;

import java.util.function.Function;

/** Generic Fusion dispatch with atomic stock fallback for every routed block. */
final class GlassentialRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final GlassentialRuntime runtime;
    private final ResourceModelRenderer stock;
    private final FusionModelEmitter emitter;
    private final GlassentialResourceExtension extension;
    private final BoundedDiagnostics diagnostics = new BoundedDiagnostics();

    GlassentialRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            GlassentialRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
        this.stock = new ResourceModelRenderer(resourcePack, textureGallery, renderSettings);
        this.emitter = new FusionModelEmitter(resourcePack, textureGallery, renderSettings);
        this.extension = BlueMap523Adapter.extension(resourcePack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant dispatch,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        Color initialMapColor = new Color().set(mapColor);
        String blockId = block.getBlockState().getId().getFormatted();
        GlassentialDefinition definition =
                Glassential345Fusion1312Profile.DEFINITIONS.get(blockId);
        FusionProgramCatalog catalog = runtime.catalog();
        if (!runtime.route().isActive() || catalog == null
                || !GlassentialStateSchema.accepts(
                        block.getBlockState(), definition
                )) {
            diagnostics.report("inactive-or-unknown-dispatch");
            renderStock(block, target, mapColor);
            return;
        }
        if (Glassential345Fusion1312Profile.COLORABLE_BLOCK_IDS.contains(blockId)
                && !validColorData(block.getBlockState(), block.getBlockEntity())) {
            diagnostics.report("inconsistent-colorable-block-entity");
            renderStock(block, target, mapColor);
            return;
        }
        try {
            boolean success = definition.modelMode() == ModelMode.ONE_WAY
                    ? renderOneWay(block, target, mapColor, catalog)
                    : renderOriginalVariants(block, target, mapColor, catalog);
            if (!success) {
                diagnostics.report("resource-render-failed");
                resetAndRenderStock(block, target, start, mapColor, initialMapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            diagnostics.report("malformed-state-or-resource");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        } catch (RuntimeException exception) {
            diagnostics.report("contained-render-failure");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        }
    }

    static boolean validColorData(BlockState state, BlockEntity blockEntity) {
        return blockEntity instanceof GlassentialColorBlockEntityData colorData
                && colorData.hasValidColorEncoding()
                && colorData.lightMatches(state);
    }

    private boolean renderOneWay(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            FusionProgramCatalog catalog
    ) {
        Direction opaqueFace = Direction.fromString(
                block.getBlockState().getProperties().get("opaque_face")
        );
        return renderVariants(
                block,
                target,
                mapColor,
                (variant, variantColor) -> renderOneWayVariant(
                        block, variant, target, variantColor, catalog, opaqueFace
                )
        );
    }

    private boolean renderOneWayVariant(
            BlockNeighborhood block,
            Variant oneWayVariant,
            TileModelView target,
            Color variantColor,
            FusionProgramCatalog catalog,
            Direction opaqueFace
    ) {
        Key baseModel = catalog.oneWayBase(oneWayVariant.getModel());
        if (baseModel == null) {
            return false;
        }
        Variant baseVariant = new Variant(new ResourcePath<Model>(baseModel));
        Model baseModelResource = baseVariant.getModel().getResource(
                resourcePack.getModels()::get
        );
        if (baseModelResource == null) {
            return false;
        }
        boolean baseAmbientOcclusion = baseModelResource.isAmbientocclusion();
        Color combined = new Color().set(0F, 0F, 0F, 0F, true);
        float[] opacity = {0F};

        Color transparentFaces = new Color().set(0F, 0F, 0F, 0F, true);
        if (!emitter.renderConnectedFaces(
                block, baseVariant, target, transparentFaces,
                catalog, opaqueFace, false
        )) {
            return false;
        }
        addVariantColor(combined, opacity, transparentFaces);

        int opaqueStart = target.getTileModel().size();
        target.initialize();
        Color opaqueColor = new Color().set(0F, 0F, 0F, 0F, true);
        MimicOutcome mimicOutcome = renderMimicFace(
                block,
                target,
                opaqueColor,
                catalog,
                opaqueFace,
                baseAmbientOcclusion
        );
        if (mimicOutcome == MimicOutcome.FAILED) {
            return false;
        }
        if (mimicOutcome == MimicOutcome.RECURSIVE) {
            target.getTileModel().reset(opaqueStart);
            target.initialize(opaqueStart);
            opaqueColor.set(0F, 0F, 0F, 0F, true);
            if (!emitter.renderConnectedFaces(
                    block, baseVariant, target, opaqueColor,
                    catalog, opaqueFace, true
            )) {
                return false;
            }
        }
        addVariantColor(combined, opacity, opaqueColor);
        if (combined.a > 0F) {
            combined.flatten().straight();
            combined.a = opacity[0];
        }
        variantColor.set(combined);
        return true;
    }

    private MimicOutcome renderMimicFace(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            FusionProgramCatalog catalog,
            Direction opaqueFace,
            boolean baseAmbientOcclusion
    ) {
        MimicSelection selection = selectMimicState(
                mimicTarget(block.getBlockEntity()), extension::defaultState
        );
        if (selection.outcome() != MimicOutcome.RENDERED) {
            return selection.outcome();
        }
        MimicBlockNeighborhood mimic = new MimicBlockNeighborhood(
                block, selection.state()
        );
        boolean rendered = renderVariants(
                mimic,
                target,
                mapColor,
                (variant, color) -> {
                    if (catalog.oneWayBase(variant.getModel()) != null) {
                        return false;
                    }
                    return catalog.get(variant.getModel()) != null
                            ? emitter.renderConnectedMimicFace(
                                    mimic, block, variant, target, color,
                                    catalog, opaqueFace, baseAmbientOcclusion
                            )
                            : emitter.renderPlainMimicFace(
                                    mimic, block, variant, target, color,
                                    opaqueFace, baseAmbientOcclusion
                            );
                }
        );
        return rendered ? MimicOutcome.RENDERED : MimicOutcome.FAILED;
    }

    static Key mimicTarget(BlockEntity blockEntity) {
        return blockEntity instanceof GlassentialOneWayBlockEntityData data
                ? data.mimicOrIron() : GlassentialOneWayBlockEntityData.IRON_BLOCK;
    }

    static MimicSelection selectMimicState(
            Key target,
            Function<Key, BlockState> registeredDefaults
    ) {
        if (target == null) {
            return new MimicSelection(MimicOutcome.RECURSIVE, null);
        }
        BlockState state = registeredDefaults.apply(target);
        if (state == null) {
            state = registeredDefaults.apply(GlassentialOneWayBlockEntityData.IRON_BLOCK);
        }
        return state == null
                ? new MimicSelection(MimicOutcome.FAILED, null)
                : new MimicSelection(MimicOutcome.RENDERED, state);
    }

    private boolean renderOriginalVariants(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            FusionProgramCatalog catalog
    ) {
        return renderVariants(
                block,
                target,
                mapColor,
                (variant, variantColor) -> emitter.render(
                        block, variant, target, variantColor, catalog
                )
        );
    }

    private boolean renderVariants(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            VariantRenderer renderer
    ) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(block.getBlockState().getId());
        if (state == null) {
            return false;
        }
        boolean[] selected = {false};
        boolean[] success = {true};
        float[] opacity = {0F};
        Color combined = new Color().set(0F, 0F, 0F, 0F, true);
        state.forEach(
                block.getBlockState(), block.getX(), block.getY(), block.getZ(),
                variant -> {
                    if (!success[0]) {
                        return;
                    }
                    selected[0] = true;
                    target.initialize();
                    Color variantColor = new Color().set(0F, 0F, 0F, 0F, true);
                    success[0] = renderer.render(variant, variantColor);
                    if (success[0]) {
                        addVariantColor(combined, opacity, variantColor);
                    }
                }
        );
        if (success[0] && combined.a > 0F) {
            combined.flatten().straight();
            combined.a = opacity[0];
        }
        if (success[0]) {
            mapColor.set(combined);
        }
        return selected[0] && success[0];
    }

    static void addVariantColor(Color combined, float[] opacity, Color variantColor) {
        opacity[0] = Math.max(opacity[0], variantColor.a);
        combined.add(variantColor.premultiplied());
    }

    private void resetAndRenderStock(
            BlockNeighborhood block,
            TileModelView target,
            int start,
            Color mapColor,
            Color initialMapColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialMapColor);
        renderStock(block, target, mapColor);
    }

    private void renderStock(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        renderVariants(block, target, mapColor, (variant, variantColor) -> {
            stock.render(block, variant, target, variantColor);
            return true;
        });
    }

    enum MimicOutcome {
        RENDERED,
        RECURSIVE,
        FAILED
    }

    record MimicSelection(MimicOutcome outcome, BlockState state) {
    }

    @FunctionalInterface
    private interface VariantRenderer {
        boolean render(Variant variant, Color mapColor);
    }
}
