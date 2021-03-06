/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.security;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class CryptPasswordTest {

    private final CryptPassword cut = new CryptPassword();

    @Test
    void encodedPasswordShouldMatchTheInput() {
        final String rawPasswd = "some.strong.password";
        final String result = cut.encode(rawPasswd);

        assertThat(cut.matches(rawPasswd, result)).isTrue();
    }

    @Test
    void testCompatibility() {
        final String rawPasswd = "some.strong.password";
        final String encodedWithPreviousVersion = "$2a$10$LpP1jatprzTm9c4gX.jx7.k3.sa7Nm2aI7pe3hY/n6ZSo6g1Zye4K";

        assertThat(cut.matches(rawPasswd, encodedWithPreviousVersion)).isTrue();
    }

    @Test
    void differentPasswordShouldNotMatchTheInput() {
        final String rawPasswd = "some.strong.password";
        final String result = cut.encode("different.password");

        assertThat(cut.matches(rawPasswd, result)).isFalse();
    }
}