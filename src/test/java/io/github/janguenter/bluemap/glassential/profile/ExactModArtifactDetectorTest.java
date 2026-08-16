/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactModArtifactDetectorTest {

    @Test
    void acceptsOnlyTheUniqueExactDualArtifactTuple(@TempDir Path temporary)
            throws IOException {
        Path glassential = required("glassentialJar");
        Path fusion = required("fusionJar");
        assertTrue(ExactModArtifactDetector.matchesRequiredPair(List.of(glassential, fusion)));
        assertFalse(ExactModArtifactDetector.matchesRequiredPair(List.of(glassential)));
        Path duplicate = temporary.resolve("duplicate-glassential.jar");
        Files.copy(glassential, duplicate);
        assertFalse(ExactModArtifactDetector.matchesRequiredPair(List.of(
                glassential, duplicate, fusion
        )));
    }

    private static Path required(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new AssertionError("missing exact test artifact property: " + property);
        }
        return Path.of(value);
    }
}
