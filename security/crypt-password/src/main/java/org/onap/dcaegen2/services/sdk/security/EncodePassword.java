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

    public static final int MAX_PASSWORD_LENGTH = 64 * 1024;
    private CryptPassword cryptPassword = new CryptPassword();
    private CharsFromStreamReader charsFromStreamReader = new CharsFromStreamReader(MAX_PASSWORD_LENGTH);

    public static void main(String[] args) {
        new EncodePassword().run(args);
    }

    public void run(String[] args) {
        try {
            encodeRawInput(readPassword(args));
        } catch (IOException ex) {
            printErrorAndExit(2, "Error while reading the password: " + ex.getMessage());
        }
    }

    private void encodeRawInput(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            printErrorAndExit(1, "Password cannot be empty");
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
        return args.length >= 1
                ? args[0]
                : charsFromStreamReader.readPasswordFromStdIn();
    }

    private void printWarning(String msg) {
        System.err.println(msg);
    }

    private void printErrorAndExit(int exitCode, String msg) {
        System.err.println(msg);
        System.exit(exitCode);
    }

    private void printResult(String encodedPassword) {
        System.out.println(encodedPassword);
    }
}
