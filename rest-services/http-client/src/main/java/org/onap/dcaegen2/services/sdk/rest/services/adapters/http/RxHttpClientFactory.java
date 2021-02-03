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

import io.netty.handler.ssl.SslContext;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ConnectionPoolConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RxHttpClientConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.TrustStoreKeys;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
public final class RxHttpClientFactory {

    private static final SslFactory SSL_FACTORY = new SslFactory();

    private RxHttpClientFactory() {
    }

    public static RxHttpClient create() {
        return new RxHttpClient(HttpClient.create());
    }

    public static RxHttpClient create(RxHttpClientConfig config){
        return Option.of(config.connectionPool())
                .map(RxHttpClientFactory::createConnectionProvider)
                .map(provider -> createWithConfig(HttpClient.create(provider), config))
                .getOrElse(createWithConfig(HttpClient.create(), config));
    }

    public static RxHttpClient create(SecurityKeys securityKeys) {
        final SslContext context = SSL_FACTORY.createSecureClientContext(securityKeys);
        return create(context);
    }

    public static RxHttpClient create(SecurityKeys securityKeys, RxHttpClientConfig config) {
        final SslContext context = SSL_FACTORY.createSecureClientContext(securityKeys);
        return create(context, config);
    }

    public static RxHttpClient create(TrustStoreKeys trustStoreKeys) {
        final SslContext context = SSL_FACTORY.createSecureClientContext(trustStoreKeys);
        return create(context);
    }

    public static RxHttpClient create(TrustStoreKeys trustStoreKeys, RxHttpClientConfig config) {
        final SslContext context = SSL_FACTORY.createSecureClientContext(trustStoreKeys);
        return create(context, config);
    }

    public static RxHttpClient createInsecure() {
        final SslContext context = SSL_FACTORY.createInsecureClientContext();
        return create(context);
    }

    public static RxHttpClient createInsecure(RxHttpClientConfig config) {
        final SslContext context = SSL_FACTORY.createInsecureClientContext();
        return create(context, config);
    }

    private static RxHttpClient create(@NotNull SslContext sslContext) {
        HttpClient secure = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        return new RxHttpClient(secure);
    }

    private static RxHttpClient create(@NotNull SslContext sslContext, RxHttpClientConfig config) {
        HttpClient secure = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        return createWithConfig(secure, config);
    }

    private static RxHttpClient createWithConfig(HttpClient httpClient, RxHttpClientConfig config) {
        return Option.of(config.retryConfig())
                .map(retryConfig -> new RxHttpClient(httpClient, retryConfig))
                .getOrElse(() -> new RxHttpClient(httpClient));
    }

    @NotNull
    private static ConnectionProvider createConnectionProvider(ConnectionPoolConfig connectionPoolConfig) {
        return ConnectionProvider.builder("fixed")
                .maxConnections(connectionPoolConfig.connectionPool())
                .maxIdleTime(connectionPoolConfig.maxIdleTime())
                .maxLifeTime(connectionPoolConfig.maxLifeTime())
                .build();
    }
}
