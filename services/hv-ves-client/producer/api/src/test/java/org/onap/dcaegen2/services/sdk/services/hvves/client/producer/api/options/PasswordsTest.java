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

import io.vavr.control.Try;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.security.ssl.Password;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since January 2019
 */
class PasswordsTest {

    @Test
    void fromFile() {
        // given
        final File file = new File("./src/test/resources/password.txt");

        // when
        final Try<Password> result = Passwords.fromFile(file);

        // then
        assertSuccessful(result);
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    @Test
    void fromPath() throws URISyntaxException {
        // given
        final Path path = Paths.get(PasswordsTest.class.getResource("/password.txt").toURI());

        // when
        final Try<Password> result = Passwords.fromPath(path);

        // then
        assertSuccessful(result);
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    @Test
    void fromPath_shouldFail_whenNotFound() {
        // given
        final Path path = Paths.get("/", UUID.randomUUID().toString());

        // when
        final Try<Password> result = Passwords.fromPath(path);

        // then
        assertThat(result.isFailure()).describedAs("Try.failure?").isTrue();
        assertThat(result.getCause()).isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void fromResource() {
        // given
        final String resource = "/password.txt";

        // when
        final Try<Password> result = Passwords.fromResource(resource);

        // then
        assertSuccessful(result);
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    private void assertSuccessful(Try<Password> result) {
        assertThat(result.isSuccess()).describedAs("Try.success?").isTrue();
    }

    private String extractPassword(Try<Password> result) {
        return result.flatMap(pass -> pass.useChecked(String::new)).get();
    }
}