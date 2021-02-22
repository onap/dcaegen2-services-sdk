/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.sdk.security;

import java.io.IOException;

class EncodePassword {

    private static final int MAX_PASSWORD_LENGTH = 64 * 1024;
    private static final int ARGS_LENGTH_PASSWORD_PROVIDED = 1;
    private final CryptPassword cryptPassword;
    private final CharsFromStreamReader charsFromStreamReader;

    public EncodePassword(CryptPassword cryptPassword, CharsFromStreamReader charsFromStreamReader) {
        this.cryptPassword = cryptPassword;
        this.charsFromStreamReader = charsFromStreamReader;
    }


    public static void main(String[] args) {
        new EncodePassword(new CryptPassword(), new CharsFromStreamReader(MAX_PASSWORD_LENGTH)).run(args);
    }

    public void run(String[] args) {
        try {
            encodeRawInput(readPassword(args));
        } catch (IOException ex) {
            printErrorAndExit(ExitCode.IO_ERROR, "Error while reading the password: " + ex.getMessage());
        }
    }

    private void encodeRawInput(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            printErrorAndExit(ExitCode.INVALID_PASSWORD, "Password cannot be empty");
        } else {
            printWarningIfContainsEndlChars(rawPassword);
            printResult(cryptPassword.encode(rawPassword));
        }
    }

    private void printWarningIfContainsEndlChars(CharSequence rawPassword) {
        if (rawPassword.chars().anyMatch(ch -> ch == '\n' || ch == '\r')) {
            printWarning("Warning: Password contains end of lines characters.");
        }
    }

    private CharSequence readPassword(String[] args) throws IOException {
        return args.length >= ARGS_LENGTH_PASSWORD_PROVIDED
                ? args[0]
                : charsFromStreamReader.readPasswordFromStdIn();
    }

    private void printWarning(String msg) {
        System.err.println(msg);
    }

    void printErrorAndExit(ExitCode exitCode, String msg) {
        System.err.println(msg);
        System.exit(exitCode.value);
    }

    private void printResult(String encodedPassword) {
        System.out.println(encodedPassword);
    }
}
