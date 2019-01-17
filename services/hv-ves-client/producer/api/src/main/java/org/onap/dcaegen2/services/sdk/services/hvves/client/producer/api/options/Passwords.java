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

import io.vavr.control.Try;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

/**
 * Utility functions for loading passwords.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
public final class Passwords {

    private Passwords() {
    }

    public static @NotNull Try<Password> fromFile(File file) {
        return fromPath(file.toPath());
    }

    public static @NotNull Try<Password> fromPath(Path path) {
        return Try.of(() -> {
            final byte[] bytes = Files.readAllBytes(path);
            final CharBuffer password = decodeChars(bytes);
            final char[] result = convertToCharArray(password);
            return new Password(result);
        });
    }

    public static @NotNull Try<Password> fromResource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()))
                .flatMap(Passwords::fromPath);
    }

    private static @NotNull CharBuffer decodeChars(byte[] bytes) {
        try {
            return Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private static char[] convertToCharArray(CharBuffer password) {
        try {
            final char[] result = new char[password.limit()];
            password.get(result);
            return result;
        } finally {
            password.flip();
            clearBuffer(password);
        }
    }

    private static void clearBuffer(CharBuffer password) {
        while (password.remaining() > 0) {
            password.put((char) 0);
        }
    }
}
