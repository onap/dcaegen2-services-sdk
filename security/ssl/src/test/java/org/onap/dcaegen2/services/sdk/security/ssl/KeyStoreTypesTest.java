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

package org.onap.dcaegen2.services.sdk.security.ssl;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Option;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class KeyStoreTypesTest {

    @Test
    void guessType_shouldReturnExtension_forP12() {
        final Option<String> result = callGuessTypeWithFileName("file.p12");
        assertThat(result.get()).isEqualTo(KeyStoreTypes.TYPE_PKCS12);
    }

    @Test
    void guessType_shouldReturnExtension_forPkcs12() {
        final Option<String> result = callGuessTypeWithFileName("file.pkcs12");
        assertThat(result.get()).isEqualTo(KeyStoreTypes.TYPE_PKCS12);
    }

    @Test
    void guessType_shouldReturnExtension_forJks() {
        final Option<String> result = callGuessTypeWithFileName("file.jks");
        assertThat(result.get()).isEqualTo(KeyStoreTypes.TYPE_JKS);
    }

    @Test
    void guessType_shouldReturnExtension_ignoringCase() {
        final Option<String> result = callGuessTypeWithFileName("file.PKCS12");
        assertThat(result.get()).isEqualTo(KeyStoreTypes.TYPE_PKCS12);
    }

    @Test
    void guessType_shouldReturnNone_whenFileDoesNotHaveExtension() {
        final Option<String> result = callGuessTypeWithFileName("file");
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void guessType_shouldReturnNone_whenFileEndsWithDot() {
        final Option<String> result = callGuessTypeWithFileName("file.");
        assertThat(result.isEmpty()).isTrue();
    }

    private Option<String> callGuessTypeWithFileName(String fileName) {
        final Path path = Paths.get("/", "tmp", fileName);
        return KeyStoreTypes.guessType(path);
    }
}