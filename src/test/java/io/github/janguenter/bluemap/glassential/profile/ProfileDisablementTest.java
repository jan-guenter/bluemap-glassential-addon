/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDisablementTest {

    @Test
    void mergesNormalizedPropertyAndEnvironmentLists() {
        ProfileDisablement disabled = ProfileDisablement.from(
                "glassential-fusion-3.4.5-1.3.12, invalid value",
                "GLASSENTIAL-FUSION-3.4.5-1.3.12,other"
        );
        assertTrue(disabled.isDisabled(Glassential345Fusion1312Profile.PROFILE_ID));
        assertEquals(2, disabled.disabledProfiles().size());
    }
}
