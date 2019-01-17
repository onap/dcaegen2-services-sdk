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

package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.Array;
import io.vavr.collection.Stream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static io.vavr.API.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class PasswordTest {

    @Test
    void use_shouldInvokeConsumerWithStoredPassword() throws Exception {
        // given
        final String password = "hej ho";
        final Password cut = new Password(password.toCharArray());
        AtomicReference<String> callArgument = new AtomicReference<>();

        // when
        cut.use((pass) -> {
            callArgument.set(new String(pass));
        });

        // then
        assertThat(callArgument.get()).isEqualTo(password);
    }

    @Test
    void use_shouldClearPasswordAfterUse() throws Exception {
        // given
        final char[] passwordChars = "hej ho".toCharArray();
        final Password cut = new Password(passwordChars);

        // when
        cut.use((pass) -> {
            // do something
        });

        // then
        assertAllCharsAreNull(passwordChars);
    }

    @Test
    void use_shouldFail_whenItWasAlreadyCalled() throws Exception {
        // given
        final Password cut = new Password("ala ma kota".toCharArray());

        // when & then
        cut.use((pass) -> {
            // do something
        });

        assertThatExceptionOfType(GeneralSecurityException.class).isThrownBy(() -> {
            cut.use((pass) -> {
                // do something
            });
        });
    }

    @Test
    void use_shouldFail_whenItWasCleared() {
        // given
        final Password cut = new Password("ala ma kota".toCharArray());

        // when & then
        cut.clear();

        assertThatExceptionOfType(GeneralSecurityException.class).isThrownBy(() -> {
            cut.use((pass) -> {
                // do something
            });
        });
    }

    @Test
    void clear_shouldClearThePassword() {        // given
        final char[] passwordChars = "hej ho".toCharArray();
        final Password cut = new Password(passwordChars);

        // when
        cut.clear();

        // then
        assertAllCharsAreNull(passwordChars);
    }

    private void assertAllCharsAreNull(char[] passwordChars) {
        assertThat(Array.ofAll(passwordChars).forAll(ch -> ch == '\0'))
                .describedAs("all characters in " + Arrays.toString(passwordChars) + " should be == '\\0'")
                .isTrue();
    }
}