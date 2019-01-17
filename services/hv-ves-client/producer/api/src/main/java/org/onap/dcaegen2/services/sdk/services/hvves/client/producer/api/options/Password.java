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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

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
     * After consumption following uses of this method will throw GeneralSecurityException.
     *
     * @param consumer of the password
     * @throws GeneralSecurityException when underlying consumer throws it or when password had been already used
     * @throws IOException when underlying consumer throws it
     */
    public void use(PasswordConsumer consumer) throws GeneralSecurityException, IOException {
        if (value == null)
            throw new GeneralSecurityException("Password had been already used so it is in cleared state");

        try {
            consumer.accept(value);
        } finally {
            clear();
        }
    }

    public void clear() {
        Arrays.fill(value, (char) 0);
        value = null;
    }

    @FunctionalInterface
    public interface PasswordConsumer {

        void accept(char[] passwordChars) throws GeneralSecurityException, IOException;
    }
}
