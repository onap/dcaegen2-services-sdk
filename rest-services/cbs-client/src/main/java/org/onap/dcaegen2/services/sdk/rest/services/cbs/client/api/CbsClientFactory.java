/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
 * Copyright (C) 2021 Wipro Limited.
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
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.CbsClientConfigMap;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.security.ssl.TrustStoreKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * <p>
 * Factory for Config Binding Service client.
 * </p>
 *
 * @since 1.1.2
 */
public class CbsClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientFactory.class);

    /**
     * <p>Creates Mono which will emit instance of {@link CbsClient} when service discovery is complete.</p>
     *
     * <p>
     * This method will do a lookup of Config Binding Service and create client configured with found address.
     * Created client will be published in returned Mono instance.
     * </p>
     * <p>
     * In case of failure during CBS resolution, returned Mono will emit error signal with possible cause.
     * User is expected to handle this signal and possibly retry subscription to returned Mono.
     * </p>
     *
     * @param configuration required CBS configuration as viewed by client application
     * @return non-null {@link Mono} of {@link CbsClient} instance
     * @since 1.1.2
     */
    public static @NotNull Mono<CbsClient> createCbsClient(CbsClientConfiguration configuration) {
        LOGGER.info("Configuration used for CBS Client: {}", configuration);
        return Mono.fromCallable(() -> buildHttpClient(configuration.trustStoreKeys()))
            .cache()
            .flatMap(httpClient -> createCbsClientMono(httpClient, configuration));
    }

    private static RxHttpClient buildHttpClient(TrustStoreKeys trustStoreKeys) {
        return trustStoreKeys != null
                ? RxHttpClientFactory.create(trustStoreKeys)
                : RxHttpClientFactory.create();
    }

    private static Mono<CbsClient> createCbsClientMono(RxHttpClient httpClient,
        CbsClientConfiguration configuration) {
            CbsClientConfigMap cbsClientConfigMap = new CbsClientConfigMap(configuration.configMapFilePath(),
                    configuration.policySyncFilePath(), configuration.appName());
            
        return Mono.just(cbsClientConfigMap);
    }
}
