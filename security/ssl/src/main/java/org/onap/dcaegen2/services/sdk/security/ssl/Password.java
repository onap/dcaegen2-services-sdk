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

import io.vavr.CheckedFunction1;
import io.vavr.Function1;
import io.vavr.control.Try;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.PasswordEvictedException;

/**
 * Simple password representation.
 *
 * A password can be used only once. After it the corresponding memory is zeroed.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
public class Password {

    private char[] value;

    public Password(@NotNull char[] value) {
        this.value = value;
    }

    /**
     * Consume the password.
     *
     * After consumption following uses of this method will return Failure(GeneralSecurityException).
     *
     * @param user of the password
     */
    public <T> T use(Function1<char[], T> user) {
        if (value == null) {
            throw new PasswordEvictedException("Password had been already used so it is in cleared state");
        }

        try {
            return user.apply(value);
        } finally {
            clear();
        }
    }

    public <T> Try<T> useChecked(CheckedFunction1<char[], T> user) {
        return use(CheckedFunction1.liftTry(user));
    }

    public void clear() {
        Arrays.fill(value, (char) 0);
        value = null;
    }

    /**
     * For security reasons this will return a constant value.
     *
     * @return some predefined string not containing the actual password
     */
    @Override
    public String toString() {
        return "<password>";
    }
}
