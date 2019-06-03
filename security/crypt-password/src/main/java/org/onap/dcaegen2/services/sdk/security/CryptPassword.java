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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Class for encoding passwords using BCrypt algorithm.
 */
public final class CryptPassword {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encode the raw password.
     *
     * @param rawPassword raw password to be encoded
     * @return encoded password
     */
    public String encode(CharSequence rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify the encoded password matches the submitted raw password. Returns true if the passwords match, false if
     * they do not.
     *
     * @param rawPassword the raw password to encode and match
     * @param encodedPassword the encoded password to compare with
     * @return true if the raw password, after encoding, matches the encoded password
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
