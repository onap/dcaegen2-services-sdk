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
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * DMaaP parameters specific for Data Router subscribers and consumers
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */

public interface DataRouterInfo extends DmaapInfo {

    /**
     * Credentials used by publisher to authenticate to Data Router
     *
     * @return configured Data Router credentials
     * @since 1.1.2
     */
    @Nullable
    DataRouterCredentials dataRouterCredentials();

    /**
     * URL to which the publisher makes Data Router publish requests
     *
     * @return configured publish URL
     * @since 1.1.2
     */
    @Nullable
    URL publishURL();

    /**
     * URL from which log data for the feed can be obtained
     *
     * @return configured log data URL
     * @since 1.1.2
     */
    @Nullable
    URL logURL();

    /**
     * URL to which the Data Router should deliver files
     *
     * @return configured files delivery URL
     * @since 1.1.2
     */
    @Nullable
    URL deliveryURL();

    /**
     * Subscriber id in Data Router
     *
     * @return configured id of subscriber
     * @since 1.1.2
     */
    @Nullable
    String subscriberId();

    /**
     * Publisher id in Data Router
     *
     * @return configured publisher id
     * @since 1.1.2
     */
    @Nullable
    String publisherId();

    @Value.Check
    default void validate() {
        if (mode() == Mode.PUBLISHER) {
            if (publishURL() == null) {
                throw new IllegalArgumentException("PublishURL need to bve set for publisher ");
            }
        }
    }

}