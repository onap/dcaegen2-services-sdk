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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class CharsFromStreamReader {
    private final int maxLength;

    public CharsFromStreamReader(int maxLength) {
        this.maxLength = maxLength;
    }

    public CharSequence readPasswordFromStdIn() throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            return readPassword(bufferedReader);
        }
    }

    public CharSequence readPassword(BufferedReader bufferedReader) throws IOException {
        final CharBuffer charBuffer = CharBuffer.allocate(maxLength);
        if (readAllChars(charBuffer, bufferedReader)) {
            charBuffer.flip();
            return charBuffer.asReadOnlyBuffer();
        } else {
            throw new IOException(
                    "Input exceeds maximum supported length of " + maxLength + " characters");
        }
    }

    private boolean readAllChars(CharBuffer charBuffer, BufferedReader bufferedReader) throws IOException {
        int readChars = 0;
        while (readChars != -1 && charBuffer.remaining() > 0) {
            readChars = bufferedReader.read(charBuffer);
        }
        // true when all characters were read
        return readChars == -1;
    }
}
