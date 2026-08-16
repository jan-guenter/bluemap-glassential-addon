/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Hash-locked roster of blockstate-selected model programs. */
public final class DirectModelCatalog {

    private static final int MAX_BYTES = 128 * 1024;
    private final Map<Key, Kind> entries;

    private DirectModelCatalog(Map<Key, Kind> entries) {
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    public static DirectModelCatalog load(
            String resource,
            int expectedRows,
            String expectedSha256
    ) {
        byte[] raw = DefinitionCatalog.read(resource, MAX_BYTES);
        if (!expectedSha256.equals(DefinitionCatalog.sha256(raw))) {
            throw new IllegalStateException("direct-model catalog integrity mismatch");
        }
        Map<Key, Kind> entries = new LinkedHashMap<>();
        String previous = null;
        for (String line : new String(raw, StandardCharsets.US_ASCII).split("\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 2) {
                throw new IllegalStateException("direct-model catalog row shape changed");
            }
            Key key = Key.parse(fields[0]);
            Kind kind = Kind.parse(fields[1]);
            if (previous != null && previous.compareTo(key.getFormatted()) >= 0) {
                throw new IllegalStateException("direct-model catalog is not sorted");
            }
            if (entries.put(key, kind) != null) {
                throw new IllegalStateException("direct-model catalog repeats a key");
            }
            previous = key.getFormatted();
        }
        if (entries.size() != expectedRows) {
            throw new IllegalStateException("direct-model catalog row count changed");
        }
        return new DirectModelCatalog(entries);
    }

    public Map<Key, Kind> entries() {
        return entries;
    }

    public Set<Key> keys() {
        return entries.keySet();
    }

    public Set<Key> keys(Kind kind) {
        Set<Key> result = new LinkedHashSet<>();
        entries.forEach((key, value) -> {
            if (value == kind) {
                result.add(key);
            }
        });
        return Collections.unmodifiableSet(result);
    }

    /** Selected runtime program kind. */
    public enum Kind {
        FUSION("fusion"),
        ONE_WAY("one_way");

        private final String wireName;

        Kind(String wireName) {
            this.wireName = wireName;
        }

        private static Kind parse(String value) {
            for (Kind kind : values()) {
                if (kind.wireName.equals(value)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("unknown direct-model kind");
        }
    }
}
