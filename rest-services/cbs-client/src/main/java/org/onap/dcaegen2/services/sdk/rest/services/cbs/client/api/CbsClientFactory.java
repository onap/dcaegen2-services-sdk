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

import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.CbsClientImpl;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.CbsLookup;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import reactor.core.publisher.Mono;

/**
 * <p>
 * Factory for Config Binding Service client.
 * </p>
 *
 * @since 1.1.2
 */
public class CbsClientFactory {

    /**
     * <p>Creates Mono which will emit instance of {@link CbsClient} when service discovery is complete.</p>
     *
     * <p>
     * This method will do a lookup of Config Binding Service using Consul as service discovery mechanism and create
     * client configured with found address. Created client will be published in returned Mono instance.
     * </p>
     * <p>
     * In case of failure during CBS resolution, returned Mono will emit error signal with possible cause.
     * User is expected to handle this signal and possibly retry subscription to returned Mono.
     * </p>
     *
     * @param env required environment properties
     * @return non-null {@link Mono} of {@link CbsClient} instance
     * @since 1.1.2
     */
    public static @NotNull Mono<CbsClient> createCbsClient(EnvProperties env) {
        return Mono.defer(() -> {
            final RxHttpClient httpClient = RxHttpClient.create();
            final CbsLookup lookup = new CbsLookup(httpClient);
            return lookup.lookup(env)
                    .map(addr -> CbsClientImpl.create(httpClient, addr, env.appName()));
        });
    }
}
