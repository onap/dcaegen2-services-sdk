/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class EncodePasswordTest {

    public static final String PASSWORD_CANNOT_BE_EMPTY = "Password cannot be empty";
    public static final String PASSWORD_TO_ENCODE = "testPass";
    public static final String EMPTY_PASSWORD = "";
    private final CryptPassword cryptPassword = mock(CryptPassword.class);
    private final CharsFromStreamReader charsFromStreamReader = mock(CharsFromStreamReader.class);
    private final EncodePassword encodePassword = spy(new EncodePassword(cryptPassword, charsFromStreamReader));

    @BeforeEach
    void setUp() {
        doNothing().when(encodePassword).printErrorAndExit(any(), any());
    }

    @Test
    void shouldExitWithErrorCodeWhenPasswordIsNotAvailableInArgs() throws IOException {
        // when
        encodePassword.run(new String[]{});

        // then
        verify(charsFromStreamReader).readPasswordFromStdIn();
        verify(encodePassword).printErrorAndExit(
                eq(ExitCode.INVALID_PASSWORD),
                eq(PASSWORD_CANNOT_BE_EMPTY)
        );
    }

    @Test
    void shouldExitWithErrorCodeWhenPasswordIsEmpty() {
        // when
        encodePassword.run(new String[]{EMPTY_PASSWORD});

        // then
        verify(encodePassword).printErrorAndExit(
                eq(ExitCode.INVALID_PASSWORD),
                eq(PASSWORD_CANNOT_BE_EMPTY)
        );
    }

    @Test
    void shouldEncodePasswordWhenPasswordIsAvailableInArgs() throws IOException {
        // when
        encodePassword.run(new String[]{PASSWORD_TO_ENCODE});

        // then
        verify(charsFromStreamReader, never()).readPasswordFromStdIn();
        verify(cryptPassword).encode(eq(PASSWORD_TO_ENCODE));
        verify(encodePassword, never()).printErrorAndExit(
                eq(ExitCode.INVALID_PASSWORD),
                eq(PASSWORD_CANNOT_BE_EMPTY)
        );
    }
}
