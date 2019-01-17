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
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since January 2019
 */
public class Password {

    private final char[] value;

    public Password(@NotNull char[] value) {
        this.value = value;
    }

    public void use(PasswordConsumer consumer) throws GeneralSecurityException, IOException {
        try {
            consumer.accept(value);
        } finally {
            clear();
        }
    }

    public void clear() {
        Arrays.fill(value, (char) 0);
    }

    @FunctionalInterface
    public interface PasswordConsumer {

        void accept(char[] passwordChars) throws GeneralSecurityException, IOException;
    }
}
