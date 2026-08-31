/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.glassential.profile.Glassential345Fusion1312Profile;
import io.github.janguenter.bluemap.glassential.profile.ResourceManifest;
import io.github.janguenter.bluemap.glassential.profile.TextureCatalog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Validates the first-wins active exact resource closure and compiles its model programs. */
final class ActiveResourceSchemaValidator {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int MAX_MODEL_BYTES = 256 * 1024;
    private static final int MAX_TEXTURE_BYTES = 4 * 1024 * 1024;
    private static final byte[] PNG_SIGNATURE = {
        (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };
    private static final Capture INVALID_CAPTURE = new Capture(false, null, false);

    private ActiveResourceSchemaValidator() {
    }

    static Result validate(
            ResourcePack resourcePack,
            Iterable<Path> roots,
            ResourceManifest manifest,
            TextureCatalog textures
    ) throws IOException, InterruptedException {
        Map<String, Capture> active = new LinkedHashMap<>();
        Map<String, Capture> activeHostResources = new LinkedHashMap<>();
        for (Path root : roots) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            resourcePack.loadResourcePath(root, activeRoot -> {
                collect(activeRoot, manifest, textures, active, false);
                collect(
                        activeRoot,
                        Glassential345Fusion1312Profile.HOST_RESOURCES,
                        textures,
                        activeHostResources,
                        true
                );
            });
        }
        if (active.size() != manifest.entries().size()) {
            return Result.invalid("required-resource-closure-missing");
        }
        if (activeHostResources.size()
                != Glassential345Fusion1312Profile.HOST_RESOURCES.entries().size()) {
            return Result.invalid("host-resource-abi-missing");
        }
        for (Capture capture : activeHostResources.values()) {
            if (!capture.valid()) {
                return Result.invalid("host-resource-abi-mismatch");
            }
        }

        Map<String, byte[]> models = new HashMap<>();
        Set<Key> pixelOverrides = new LinkedHashSet<>();
        for (ResourceManifest.Entry entry : manifest.entries().values()) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Capture capture = active.get(entry.path());
            if (capture == null || !capture.valid()) {
                return Result.invalid("active-resource-integrity-mismatch");
            }
            if (entry.kind().equals("model")) {
                models.put(entry.path(), capture.modelBytes());
            } else if (entry.kind().equals("texture") && capture.pixelOverride()) {
                pixelOverrides.add(textureKey(entry.path()));
            }
        }
        try {
            return Result.success(FusionProgramCatalog.parse(
                    models,
                    Glassential345Fusion1312Profile.FUSION_MODEL_KEYS,
                    Glassential345Fusion1312Profile.ONE_WAY_MODEL_KEYS
            ), pixelOverrides);
        } catch (IllegalArgumentException exception) {
            return Result.invalid("active-fusion-schema-mismatch");
        }
    }

    static void collect(
            Path root,
            ResourceManifest manifest,
            TextureCatalog textures,
            Map<String, Capture> active,
            boolean exactBytes
    ) throws IOException {
        for (ResourceManifest.Entry entry : manifest.entries().values()) {
            String path = entry.path();
            if (active.containsKey(path)) {
                continue;
            }
            Path candidate = root.resolve(path);
            if (Files.isRegularFile(candidate)) {
                claimFirstWinner(
                        active,
                        path,
                        () -> capture(candidate, entry, textures, exactBytes)
                );
            }
        }
    }

    static void claimFirstWinner(
            Map<String, Capture> active,
            String path,
            CaptureReader reader
    ) throws IOException {
        if (active.containsKey(path)) {
            return;
        }
        // Claim the physical winner before reading so a broken higher-priority
        // archive entry can never fall through to a later resource-pack root.
        active.put(path, INVALID_CAPTURE);
        active.put(path, reader.read());
    }

    private static Capture capture(
            Path resource,
            ResourceManifest.Entry entry,
            TextureCatalog textures,
            boolean exactBytes
    ) throws IOException {
        if (exactBytes) {
            boolean valid = Files.size(resource) == entry.size()
                    && entry.sha256().equals(sha256(resource));
            return new Capture(valid, null, false);
        }
        if (entry.kind().equals("texture")) {
            TextureCatalog.Entry texture = textures.get(textureKey(entry.path()));
            boolean valid = texture != null && validTexture(resource, texture);
            boolean pixelOverride = valid && !entry.sha256().equals(sha256(resource));
            return new Capture(valid, null, pixelOverride);
        }
        boolean valid = Files.size(resource) == entry.size()
                && entry.sha256().equals(sha256(resource));
        if (!valid || !entry.kind().equals("model")) {
            return new Capture(valid, null, false);
        }
        if (entry.size() > MAX_MODEL_BYTES) {
            return new Capture(false, null, false);
        }
        return new Capture(true, Files.readAllBytes(resource), false);
    }

    static boolean validTexture(Path resource, TextureCatalog.Entry texture) throws IOException {
        if (Files.size(resource) > MAX_TEXTURE_BYTES) {
            return false;
        }
        byte[] header;
        try (InputStream input = Files.newInputStream(resource)) {
            header = input.readNBytes(24);
        }
        if (!hasExpectedPngHeader(header, texture.width(), texture.height())) {
            return false;
        }
        BufferedImage image;
        try (InputStream input = Files.newInputStream(resource)) {
            image = ImageIO.read(input);
        }
        return image != null && image.getWidth() == texture.width()
                && image.getHeight() == texture.height();
    }

    static boolean hasExpectedPngHeader(byte[] header, int width, int height) {
        if (header.length != 24) {
            return false;
        }
        for (int index = 0; index < PNG_SIGNATURE.length; index++) {
            if (header[index] != PNG_SIGNATURE[index]) {
                return false;
            }
        }
        return readInt(header, 8) == 13
                && header[12] == 'I' && header[13] == 'H'
                && header[14] == 'D' && header[15] == 'R'
                && readInt(header, 16) == width
                && readInt(header, 20) == height;
    }

    private static int readInt(byte[] value, int offset) {
        return (value[offset] & 0xff) << 24
                | (value[offset + 1] & 0xff) << 16
                | (value[offset + 2] & 0xff) << 8
                | value[offset + 3] & 0xff;
    }

    private static Key textureKey(String path) {
        String prefix = "assets/glassential/textures/";
        if (!path.startsWith(prefix) || !path.endsWith(".png")) {
            throw new IllegalArgumentException("malformed texture manifest path");
        }
        return Key.parse("glassential:" + path.substring(prefix.length(), path.length() - 4));
    }

    private static String sha256(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    record Result(
            boolean valid,
            String reason,
            FusionProgramCatalog catalog,
            Set<Key> pixelOverrides
    ) {

        private static Result success(
                FusionProgramCatalog catalog,
                Set<Key> pixelOverrides
        ) {
            return new Result(
                    true, "exact-active-schema", catalog, Set.copyOf(pixelOverrides)
            );
        }

        private static Result invalid(String reason) {
            return new Result(false, reason, null, Set.of());
        }
    }

    @FunctionalInterface
    interface CaptureReader {
        Capture read() throws IOException;
    }

    record Capture(boolean valid, byte[] modelBytes, boolean pixelOverride) {
    }
}
