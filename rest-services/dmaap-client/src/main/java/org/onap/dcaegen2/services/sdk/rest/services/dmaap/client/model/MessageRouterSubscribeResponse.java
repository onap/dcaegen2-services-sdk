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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model;


import com.google.gson.JsonArray;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@ExperimentalApi
@Value.Immutable
public interface MessageRouterSubscribeResponse extends DmaapResponse {

    @Value.Default
    default JsonArray items() { return new JsonArray(); }

    @Value.Derived
    default boolean hasElements() {
        return items().size() > 0;
    }

    @Value.Derived
    default boolean isEmpty() {
        return !hasElements();
    }
}
