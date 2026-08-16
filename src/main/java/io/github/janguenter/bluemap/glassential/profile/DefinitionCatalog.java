/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict immutable loader for the packaged metadata-only route catalog. */
public final class DefinitionCatalog {

    private static final int MAX_BYTES = 1024 * 1024;

    private final Map<String, GlassentialDefinition> definitions;

    private DefinitionCatalog(Map<String, GlassentialDefinition> definitions) {
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
    }

    public static DefinitionCatalog load(String resource, int expectedRows, String expectedSha256) {
        byte[] raw = read(resource, MAX_BYTES);
        if (!expectedSha256.equals(sha256(raw))) {
            throw new IllegalStateException("definition catalog integrity mismatch");
        }
        String text = new String(raw, StandardCharsets.US_ASCII);
        if (!text.endsWith("\n")) {
            throw new IllegalStateException("definition catalog is not LF-terminated");
        }
        Map<String, GlassentialDefinition> definitions = new LinkedHashMap<>();
        String previous = null;
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 7) {
                throw new IllegalStateException("definition catalog row shape changed");
            }
            ShapeFamily family = ShapeFamily.parse(fields[1]);
            GlassentialDefinition definition = new GlassentialDefinition(
                    fields[0],
                    family,
                    StateSchema.parse(fields[2]),
                    ModelMode.parse(fields[3]),
                    Integer.parseInt(fields[4]),
                    fields[5],
                    fields[6]
            );
            if (previous != null && previous.compareTo(definition.blockId()) >= 0) {
                throw new IllegalStateException("definition catalog is not sorted");
            }
            if (definitions.put(definition.blockId(), definition) != null) {
                throw new IllegalStateException("definition catalog repeats a block");
            }
            previous = definition.blockId();
        }
        if (definitions.size() != expectedRows) {
            throw new IllegalStateException("definition catalog row count changed");
        }
        return new DefinitionCatalog(definitions);
    }

    public Map<String, GlassentialDefinition> definitions() {
        return definitions;
    }

    static byte[] read(String resource, int maximum) {
        try (InputStream input = DefinitionCatalog.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("packaged profile resource is missing");
            }
            byte[] raw = input.readNBytes(maximum + 1);
            if (raw.length > maximum) {
                throw new IllegalStateException("packaged profile resource is oversized");
            }
            return raw;
        } catch (IOException exception) {
            throw new IllegalStateException("packaged profile resource is unreadable", exception);
        }
    }

    static String sha256(byte[] raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
