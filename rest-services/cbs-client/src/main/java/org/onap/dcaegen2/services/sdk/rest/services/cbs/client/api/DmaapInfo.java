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

import java.net.URL;

/**
 * Specific Configuration for apps utilizing DMaaP
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */
@Value.Immutable
public interface DmaapInfo {

    /**
     * AAF client role
     *
     * @return configured AAF client role
     * @since 1.1.2
     */
    @Nullable
    String clientRole();

    /**
     * Client id for given AAF client
     *
     * @return configured client id
     * @since 1.1.2
     */
    @Nullable
    String clientId();

    /**
     * DCAE location for publisher or subscriber
     *
     * @return configured DCAE location
     * @since 1.1.2
     */
    @Nullable
    String location();

    /**
     * A url of kafka topic
     *
     * @return configured kafka topic url
     */
    @NotNull
    URL topicURL();
}
