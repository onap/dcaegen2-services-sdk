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

/**
 * AAF credentials used by message router clients to authenticate with secure topics
 *
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 * @since 1.1.2
 */

@Value.Immutable
public interface AafCredentials {
    /**
     * AAF username message router clients use to authenticate with secure topics
     *
     * @return configured AAF username
     * @since 1.1.2
     */
    @NotNull
    abstract String aafUsername();

    /**
     * AAF password message router clients use to authenticate with secure topics
     *
     * @return configured AAF password
     * @since 1.1.2
     */
    @NotNull
    abstract String aafPassword();

    @Value.Check
    default void validate() {
        if (aafUsername() == null || aafPassword() == null) {
            throw new IllegalArgumentException("Username and Password are mandatory parameters");
        }
    }

}