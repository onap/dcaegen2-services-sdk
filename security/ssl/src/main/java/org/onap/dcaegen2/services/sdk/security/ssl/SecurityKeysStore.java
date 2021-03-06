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

import java.nio.file.Path;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.SecurityConfigurationException;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
@Value.Immutable
public interface SecurityKeysStore {
    /**
     * Stores the file path of the key store. It should contain data in format specified by {@link #type()}.
     *
     * @return key store path
     */
    @Value.Parameter
    Path path();

    /**
     * Type of the key store. Can be anything supported by the JVM, eg. {@code jks} or {@code pkcs12}.
     *
     * If not set it will be guessed from the {@link #path()}. {@link IllegalStateException} will be thrown if it will
     * not be possible.
     *
     * @return key store type
     * @throws org.onap.dcaegen2.services.sdk.security.ssl.exceptions.SecurityConfigurationException when file type is unknown
     */
    @Value.Default
    default String type() {
        return KeyStoreTypes.inferTypeFromExtension(path())
                .getOrElseThrow(() -> new SecurityConfigurationException("Could not determine key store type by file name"));
    }

    static SecurityKeysStore fromPath(Path path) {
        return ImmutableSecurityKeysStore.of(path);
    }
}
