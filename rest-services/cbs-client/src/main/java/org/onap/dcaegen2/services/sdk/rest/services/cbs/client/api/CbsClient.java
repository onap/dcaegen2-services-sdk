/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Duration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Main Config Binding Service client interface.</p>
 *
 * <p>User should use this interface to subscribe to events published when CBS client fetches configuration.</p>
 *
 * @since 1.1.2
 */
@FunctionalInterface
public interface CbsClient {

    /**
     * Get reactive configuration stream.
     * <p>
     * Returns a {@link Mono} that publishes new configuration after CBS client retrieves one.
     *
     * @param serviceComponentName url key under which CBS client should look for configuration
     * @return reactive stream of configuration
     * @since 1.1.2
     */
    @NotNull Mono<JsonElement> get(String serviceComponentName);

    default Flux<JsonElement> updates(String serviceComponentName, Duration initialDelay, Duration period) {
        return Flux.interval(initialDelay, period)
                .flatMap(i -> get(serviceComponentName));
    }
}
