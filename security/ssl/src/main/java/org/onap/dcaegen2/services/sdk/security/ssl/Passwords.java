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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.ReadingPasswordFromFileException;

/**
 * Utility functions for loading passwords.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
public final class Passwords {

    private Passwords() {
    }

    /**
     * Reads password from file.
     *
     * @param file to read
     * @return Password instance with contents of the file
     * @throws ReadingPasswordFromFileException when file could not be read
     */
    public static @NotNull Password fromFile(File file) {
        return fromPath(file.toPath());
    }

    /**
     * Reads password from file.
     *
     * @param path of the file to read
     * @return Password instance with contents of the file
     * @throws ReadingPasswordFromFileException when file could not be read
     */
    public static @NotNull Password fromPath(Path path) {
        try {
            final byte[] bytes = Files.readAllBytes(path);
            final CharBuffer password = decodeChars(bytes);
            final char[] result = convertToCharArray(password);
            return new Password(result);
        } catch (IOException e) {
            throw new ReadingPasswordFromFileException("Could not read password from " + path, e);
        }
    }


    /**
     * Reads password from resource.
     *
     * @param resource URL starting with slash
     * @return Password instance with contents of the resource
     * @throws ReadingPasswordFromFileException when resource could not be read
     */
    public static @NotNull Password fromResource(String resource) {
        final URL resourceUrl = Passwords.class.getResource(resource);
        if (resourceUrl == null) {
            throw new ReadingPasswordFromFileException("Could not find resource " + resource);
        }

        return fromPath(asPath(resourceUrl));

    }

    private static Path asPath(URL resourceUrl) {
        try {
            return Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new ReadingPasswordFromFileException("Could not read password", e);
        }
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
