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
 * DCAE apps configuration representation.
 * <p>
 * Depend on Publisher Type specific fields are required (see check() method)
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */
@Value.Immutable
public abstract class Publisher {

    /**
     * A type of Publisher
     *
     * @return configured Publisher type
     * @since 1.1.2
     */
    @NotNull
    abstract PublisherType publishersType();

    /**
     * AAF username used for authentication of secure topics
     *
     * @return configured AAF username
     * @since 1.1.2
     */
    @Nullable
    abstract String aafUsername();

    /**
     * AAF password used for authentication of secure topics
     *
     * @return configured AAF password
     * @since 1.1.2
     */
    @Nullable
    abstract String aafPassword();

    /**
     * A DMaaP settings
     *
     * @return configured DMaap settings
     * @sience 1.1.2
     */
    @Nullable
    abstract DmaapInfo dmaapInfo();

/*
    This will be place where native kafka configuration will be stored
    @Nullable
    KafkaInfo kafkaInfo();*/

    @Value.Check
    protected void check() {
        if (publishersType().equals(PublisherType.MESSAGE_ROUTER) && !dmaapInfo().equals(null)) {
            throw new IllegalArgumentException("DmaapInfo must be set if PublisherType is message_router");
        }
    }

    enum PublisherType {
        MESSAGE_ROUTER, NATIVE_KAFKA
    }

}
