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

import io.vavr.collection.Set;
import java.net.InetSocketAddress;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
@Value.Immutable
public interface ProducerOptions {

    /**
     * A set of available collector endpoints.
     *
     * @return configured collector endpoints
     * @since 1.1.1
     */
    @NotNull
    Set<InetSocketAddress> collectorAddresses();

    /**
     * Security keys definition used when connecting to the collector.
     *
     * @return security keys definition or null when plain TCP sockets are to be used.
     * @since 1.1.1
     */
    @Nullable
    SecurityKeys securityKeys();

    /**
     * Version of Wire Transfer Protocol interface frame
     *
     * @return Version of interface frame
     * @since 1.1.1
     */
    WireFrameVersion wireFrameVersion();


    @Value.Check
    default void validate() {
        if (collectorAddresses().isEmpty()) {
            throw new IllegalArgumentException("address list cannot be empty");
        }
    }

}
