/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RxHttpClientConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.retry.RetryLogicFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.TrustStoreKeys;
import reactor.netty.http.client.HttpClient;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
public final class RxHttpClientFactory {

    private RxHttpClientFactory() {
    }

    public static RxHttpClient create() {
        return new RxHttpClient(HttpClientFactory.create());
    }

    public static RxHttpClient create(RxHttpClientConfig config) {
        HttpClient httpClient = Option.of(config.connectionPool())
                .map(HttpClientFactory::create)
                .getOrElse(HttpClientFactory::create);
        return createWithConfig(httpClient, config);
    }

    public static RxHttpClient create(SecurityKeys securityKeys) {
        return new RxHttpClient(HttpClientFactory.create(securityKeys));
    }

    public static RxHttpClient create(SecurityKeys securityKeys, RxHttpClientConfig config) {
        HttpClient httpClient = Option.of(config.connectionPool())
                .map(connectionPoolConfig -> HttpClientFactory.create(securityKeys, connectionPoolConfig))
                .getOrElse(() -> HttpClientFactory.create(securityKeys));
        return createWithConfig(httpClient, config);
    }

    public static RxHttpClient create(TrustStoreKeys trustStoreKeys) {
        return new RxHttpClient(HttpClientFactory.create(trustStoreKeys));
    }

    public static RxHttpClient create(TrustStoreKeys trustStoreKeys, RxHttpClientConfig config) {
        HttpClient httpClient = Option.of(config.connectionPool())
                .map(connectionPoolConfig -> HttpClientFactory.create(trustStoreKeys, connectionPoolConfig))
                .getOrElse(() -> HttpClientFactory.create(trustStoreKeys));
        return createWithConfig(httpClient, config);
    }

    public static RxHttpClient createInsecure() {
        return new RxHttpClient(HttpClientFactory.createInsecure());
    }

    public static RxHttpClient createInsecure(RxHttpClientConfig config) {
        HttpClient httpClient = Option.of(config.connectionPool())
                .map(HttpClientFactory::createInsecure)
                .getOrElse(HttpClientFactory::createInsecure);
        return createWithConfig(httpClient, config);
    }

    private static RxHttpClient createWithConfig(HttpClient httpClient, RxHttpClientConfig config) {
        return Option.of(config.retryConfig())
                .map(retryConfig -> new RxHttpClient(httpClient, RetryLogicFactory.create(retryConfig)))
                .getOrElse(() -> new RxHttpClient(httpClient));
    }
}
