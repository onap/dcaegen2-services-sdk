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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class CharsFromStreamReaderTest {
    private static final int MAX_LENGTH = 6;
    private final CharsFromStreamReader cut = new CharsFromStreamReader(MAX_LENGTH);

    @Test
    void readPasswordShouldThrowExceptionWhenInputExceedsMaxLength() throws IOException {
        try (BufferedReader input = new BufferedReader(new StringReader("very long password"))) {
            assertThatThrownBy(() -> cut.readPassword(input))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining(Integer.toString(MAX_LENGTH));
        }
    }


    @Test
    void readPasswordShouldReturnThePassword() throws IOException {
        // given
        final String givenPass = "pass";
        BufferedReader input = new BufferedReader(new StringReader(givenPass));

        // when
        final CharSequence result = cut.readPassword(input);

        // then
        assertThat(result.toString()).isEqualTo(givenPass);
    }
}