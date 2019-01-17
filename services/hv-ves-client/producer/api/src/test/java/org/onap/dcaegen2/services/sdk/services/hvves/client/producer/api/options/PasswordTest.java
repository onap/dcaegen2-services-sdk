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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.vavr.collection.Array;
import io.vavr.control.Try;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class PasswordTest {

    @Test
    void use_shouldInvokeConsumerWithStoredPassword() {
        // given
        final String password = "hej ho";
        final Password cut = new Password(password.toCharArray());

        // when
        String result = cut.useChecked(String::new).get();

        // then
        assertThat(result).isEqualTo(password);
    }

    @Test
    void use_shouldClearPasswordAfterUse() {
        // given
        final char[] passwordChars = "hej ho".toCharArray();
        final Password cut = new Password(passwordChars);

        // when
        useThePassword(cut);

        // then
        assertAllCharsAreNull(passwordChars);
    }

    @Test
    void use_shouldFail_whenItWasAlreadyCalled() {
        // given
        final Password cut = new Password("ala ma kota".toCharArray());

        // when & then
        useThePassword(cut).get();

        assertThatExceptionOfType(GeneralSecurityException.class).isThrownBy(() ->
                useThePassword(cut).get());
    }

    @Test
    void use_shouldFail_whenItWasCleared() {
        // given
        final Password cut = new Password("ala ma kota".toCharArray());

        // when & then
        cut.clear();

        assertThatExceptionOfType(GeneralSecurityException.class).isThrownBy(() ->
                useThePassword(cut).get());
    }

    @Test
    void clear_shouldClearThePassword() {
        // given
        final char[] passwordChars = "hej ho".toCharArray();
        final Password cut = new Password(passwordChars);

        // when
        cut.clear();

        // then
        assertAllCharsAreNull(passwordChars);
    }

    private Try<Object> useThePassword(Password cut) {
        return cut.use((pass) -> Try.success(42));
    }

    private void assertAllCharsAreNull(char[] passwordChars) {
        assertThat(Array.ofAll(passwordChars).forAll(ch -> ch == '\0'))
                .describedAs("all characters in " + Arrays.toString(passwordChars) + " should be == '\\0'")
                .isTrue();
    }
}