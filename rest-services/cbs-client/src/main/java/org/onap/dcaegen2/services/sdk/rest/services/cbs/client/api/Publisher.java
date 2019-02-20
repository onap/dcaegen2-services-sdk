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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * DCAE apps configuration representation (publishers side).
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */
@Value.Immutable
public interface Publisher {

    /**
     * A type of Publisher, each type has its specific fields to set
     *
     * @return configured Publisher type
     * @since 1.1.2
     */
    @NotNull
    PublisherType publisherType();


    /**
     * AAF credentials used to authenticate
     *
     * @return configured AAF credentials
     * @since 1.1.2
     */
    @Nullable
    AafCredentials aafCredentials();

    /**
     * A DMaaP settings, contains the topic connection details
     *
     * @return configured DMaap settings
     * @sience 1.1.2
     */
    @Nullable
    DmaapInfo dmaapInfo();

    /*
    This will be place where native kafka configuration will be stored
    @Nullable
    KafkaInfo kafkaInfo();
    */

    @Value.Check
    default void validate() {
        if (publisherType() == null || dmaapInfo() == null) {
            throw new IllegalArgumentException("PublisherType and DmaapInfo must be set");
        }
    }

    enum PublisherType {
        MESSAGE_ROUTER, DATA_ROUTER, NATIVE_KAFKA
    }

}
