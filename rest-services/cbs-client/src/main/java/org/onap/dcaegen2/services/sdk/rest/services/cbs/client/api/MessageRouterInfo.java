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
 * DMaaP parameters specific for Message Router subscribers and consumers
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */
@Value.Immutable
public interface MessageRouterInfo extends DmaapInfo {

    /**
     * AAF credentials used to authenticate
     *
     * @return configured AAF credentials
     * @since 1.1.2
     */
    @Nullable
    AafCredentials aafCredentials();

    /**
     * AAF client role that’s requesting publish or subscribe access to the topic
     *
     * @return configured AAF client role
     * @since 1.1.2
     */
    @Nullable
    String clientRole();

    /**
     * AAF client id
     *
     * @return configured client id
     * @since 1.1.2
     */
    @Nullable
    String clientId();

    /**
     * URL for accessing the topic to publish or receive events
     *
     * @return configured topic url
     * @since 1.1.2
     */
    @Nullable
    URL topicURL();

    @Value.Check
    default void validate() {
        if (mode() == Mode.PUBLISHER || mode() == Mode.SUBSCRIBER) {
            if (topicURL() == null) {
                throw new IllegalArgumentException("TopicUrl must be set for publisher and subscriber");
            }
        }
    }

}