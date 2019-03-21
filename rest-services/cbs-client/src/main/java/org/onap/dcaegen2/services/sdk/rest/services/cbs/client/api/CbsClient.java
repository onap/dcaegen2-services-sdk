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

import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>Main Config Binding Service client interface.</p>
 *
 * <p>User should use this interface to subscribe to events published when CBS client fetches configuration.</p>
 *
 * @since 1.1.2
 */
public interface CbsClient {

    /**
     * <p>
     * Get current application configuration.
     *
     * <p>
     * Returns a {@link Mono} that publishes new configuration after CBS client retrieves one.
     *
     * @param request the CBS Request to be performed (can be obtained from {@link CbsRequests})
     * @return reactive stream of configuration
     */
    @NotNull Mono<JsonObject> get(CbsRequest request);

    /**
     * <p>
     * Poll for configuration.
     *
     * <p>
     * Will call {@link #get(CbsRequest)} after {@code initialDelay} every {@code period}. Resulting entries may or may not be
     * changed, ie. items in the stream might be the same until change is made in CBS.
     *
     * @param request the CBS Request to be performed (can be obtained from {@link CbsRequests})
     * @param initialDelay delay after first request attempt
     * @param period frequency of update checks
     * @return stream of configuration states
     */
    default Flux<JsonObject> get(CbsRequest request, Duration initialDelay, Duration period) {
        return Flux.interval(initialDelay, period)
                .map(i -> request.withNewInvocationId())
                .flatMap(this::get);
    }

    /**
     * <p>
     * Poll for configuration updates.
     *
     * <p>
     * Will call {@link #get(CbsRequest)} after {@code initialDelay} every {@code period}. Will emit an item
     * only when an update was detected, ie. when new item is different then last emitted item.
     *
     * <p>
     * For more tailored change detection approach you can:
     * <ul>
     *     <li>
     *         Use {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.ListenableCbsConfig}
     *         (<b>experimental API</b>) if you want to react differently to changes in subsets of the configuration.
     *     </li>
     *     <li>
     *         Use {@link #get(CbsRequest, Duration, Duration)} with
     *         {@link Flux#distinctUntilChanged(Function, BiPredicate)} if you want to specify custom comparison logic.
     *     </li>
     * </ul>
     *
     * @param request the CBS Request to be performed (can be obtained from {@link CbsRequests})
     * @param initialDelay delay after first request attempt
     * @param period frequency of update checks
     * @return stream of configuration updates
     */
    default Flux<JsonObject> updates(CbsRequest request, Duration initialDelay, Duration period) {
        return get(request, initialDelay, period).distinctUntilChanged();
    }
}
