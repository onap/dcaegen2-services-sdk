/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * Copyright (c) 2023 Deutsche Telekom AG. All rights reserved.
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.ReadingPasswordFromFileException;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since January 2019
 */
class PasswordsTest {

    @Test
    void wrap() {
        // given
        final char[] passwd = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};

        // when
        final Password result = Passwords.wrap(passwd);

        // then
        assertThat(extractPassword(result)).isEqualTo("password");
        assertThat(passwd).containsOnly('\0');
    }

    @Test
    void fromString() {
        // given
        final String passwd = "password";

        // when
        final Password result = Passwords.fromString(passwd);

        // then
        assertThat(extractPassword(result)).isEqualTo("password");
    }

    @Test
    void fromFile() {
        // given
        final File file = new File("./src/test/resources/password.txt");

        // when
        final Password result = Passwords.fromFile(file);

        // then
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    @Test
    void fromFileWhenNotExisting() {
        // given
        final File file = new File("./not existing file");

        // when & then
        assertThatThrownBy(() -> Passwords.fromFile(file))
                .isInstanceOf(ReadingPasswordFromFileException.class)
                .hasCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    void fromPath() throws URISyntaxException {
        // given
        final Path path = Paths.get(PasswordsTest.class.getResource("/password.txt").toURI());

        // when
        final Password result = Passwords.fromPath(path);

        // then
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    @Test
    void fromPath_shouldFail_whenNotFound() {
        // given
        final Path path = Paths.get(File.listRoots()[0].toString(), UUID.randomUUID().toString());

        // when
        Assertions.assertThrows(ReadingPasswordFromFileException.class, () -> {
            Passwords.fromPath(path);
        });

    }

    @Test
    void fromResource() {
        // given
        final String resource = "/password.txt";

        // when
        final Password result = Passwords.fromResource(resource);

        // then
        assertThat(extractPassword(result)).isEqualTo("ja baczewski\n2nd line");
    }

    private String extractPassword(Password pass) {
        return pass.use(String::new);
    }
}