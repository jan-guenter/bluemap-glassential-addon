/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.util.Map;
import java.util.Set;

/** Exact All the Mons 1.2.0 Glassential 3.4.5/Fusion 1.3.12 profile. */
public final class Glassential345Fusion1312Profile {

    private static final String ROOT =
            "/bluemap-glassential/profiles/glassential/3.4.5-fusion-1.3.12/";

    public static final String PROFILE_ID = "glassential-fusion-3.4.5-1.3.12";
    public static final String GLASSENTIAL_SHA256 =
            "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471";
    public static final long GLASSENTIAL_SIZE = 702_249L;
    public static final String FUSION_SHA256 =
            "17f5215648a98bcde4134577b013200dbf363273ae282449c51408ae8346f2fa";
    public static final long FUSION_SIZE = 923_270L;

    public static final int ALL_GLASSENTIAL_BLOCK_COUNT = 113;
    public static final int ALL_GLASSENTIAL_STATE_COUNT = 5_449;
    public static final int ROUTED_GLASSENTIAL_BLOCK_COUNT = 31;
    public static final int ROUTED_GLASSENTIAL_STATE_COUNT = 453;
    public static final int STOCK_GLASSENTIAL_BLOCK_COUNT = 82;
    public static final int STOCK_GLASSENTIAL_STATE_COUNT = 4_996;
    public static final int VANILLA_OVERRIDE_COUNT = 18;
    public static final int ROUTED_BLOCK_COUNT = 49;
    public static final int ROUTED_STATE_COUNT = 471;
    public static final int DIRECT_MODEL_COUNT = 95;
    public static final int FUSION_PROGRAM_COUNT = 93;
    public static final int ONE_WAY_PROGRAM_COUNT = 2;
    public static final int MODEL_COUNT = 100;
    public static final int TEXTURE_COUNT = 39;
    public static final int CONNECTED_TEXTURE_COUNT = 34;
    public static final int METADATA_COUNT = 34;
    public static final int RESOURCE_COUNT = 204;
    public static final int HOST_RESOURCE_COUNT = 22;

    public static final String DEFINITIONS_SHA256 =
            "2756fd50be5fb2da5e2fb366c7b89f6a6e700361bb7369f016dcbef36834bc45";
    public static final String DIRECT_MODELS_SHA256 =
            "8736bbf9b925b7f793489ba14cc421eaf72414f7f22b70802f759d2ee24f241c";
    public static final String RESOURCES_SHA256 =
            "a6a8eb7addb5a3866ea27d91935d7501236f3b93c2536004cb9148dd7d6b8d67";
    public static final String TEXTURES_SHA256 =
            "2452dc2aba6249599c5a5eecf280a7ac4e9b704bb6cd29a7656acd1a6c7b7e3e";
    public static final String HOST_RESOURCES_SHA256 =
            "2b63c65a78eefce67a9cb51759d0f375f02ade20aa3f3c27aaad10555a9e6221";

    public static final DefinitionCatalog CATALOG = DefinitionCatalog.load(
            ROOT + "definitions.tsv", ROUTED_BLOCK_COUNT, DEFINITIONS_SHA256
    );
    public static final Map<String, GlassentialDefinition> DEFINITIONS =
            CATALOG.definitions();
    public static final Set<String> ROUTED_BLOCKS = DEFINITIONS.keySet();
    public static final Set<String> ROUTED_GLASSENTIAL_BLOCK_IDS = Set.copyOf(
            ROUTED_BLOCKS.stream().filter(id -> id.startsWith("glassential:")).toList()
    );
    public static final Set<String> VANILLA_OVERRIDE_BLOCK_IDS = Set.copyOf(
            ROUTED_BLOCKS.stream().filter(id -> id.startsWith("minecraft:")).toList()
    );

    public static final DirectModelCatalog DIRECT_MODELS = DirectModelCatalog.load(
            ROOT + "direct-models.tsv", DIRECT_MODEL_COUNT, DIRECT_MODELS_SHA256
    );
    public static final Set<Key> DIRECT_MODEL_KEYS = DIRECT_MODELS.keys();
    public static final Set<Key> FUSION_MODEL_KEYS =
            DIRECT_MODELS.keys(DirectModelCatalog.Kind.FUSION);
    public static final Set<Key> ONE_WAY_MODEL_KEYS =
            DIRECT_MODELS.keys(DirectModelCatalog.Kind.ONE_WAY);

    public static final ResourceManifest RESOURCES = ResourceManifest.load(
            ROOT + "required-resources.tsv",
            RESOURCE_COUNT,
            RESOURCES_SHA256,
            Set.of("glassential", "minecraft")
    );
    public static final TextureCatalog TEXTURES = TextureCatalog.load(
            ROOT + "textures.tsv", TEXTURE_COUNT, TEXTURES_SHA256
    );
    public static final ResourceManifest HOST_RESOURCES = ResourceManifest.load(
            ROOT + "host-resources.tsv",
            HOST_RESOURCE_COUNT,
            HOST_RESOURCES_SHA256,
            "minecraft"
    );

    public static final Set<String> COLORABLE_BLOCK_IDS = Set.of(
            "glassential:colorable_glass",
            "glassential:colorable_glass_pane",
            "glassential:colorable_stained_glass",
            "glassential:colorable_stained_glass_pane"
    );
    public static final Set<String> ONE_WAY_BLOCK_IDS = Set.of(
            "glassential:one_way_glass",
            "glassential:tinted_one_way_glass"
    );

    private Glassential345Fusion1312Profile() {
    }
}
