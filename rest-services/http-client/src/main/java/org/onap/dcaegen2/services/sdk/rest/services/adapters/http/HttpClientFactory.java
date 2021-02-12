/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ConnectionPoolConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.TrustStoreKeys;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

class HttpClientFactory {

    private static final SslFactory SSL_FACTORY = new SslFactory();

    private HttpClientFactory() {
    }

    static HttpClient create(){
        return HttpClient.create();
    }

    static HttpClient create(ConnectionPoolConfig connectionPoolConfig){
        return HttpClient.create(createConnectionProvider(connectionPoolConfig));
    }

    static HttpClient create(SecurityKeys securityKeys){
        final SslContext sslContext = SSL_FACTORY.createSecureClientContext(securityKeys);
        return HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
    }

    static HttpClient create(SecurityKeys securityKeys, ConnectionPoolConfig connectionPoolConfig){
        final SslContext sslContext = SSL_FACTORY.createSecureClientContext(securityKeys);
        final ConnectionProvider connectionProvider = createConnectionProvider(connectionPoolConfig);
        return HttpClient.create(connectionProvider).secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
    }

    static HttpClient create(TrustStoreKeys trustStoreKeys){
        final SslContext sslContext = SSL_FACTORY.createSecureClientContext(trustStoreKeys);
        return HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
    }

    static HttpClient create(TrustStoreKeys trustStoreKeys, ConnectionPoolConfig connectionPoolConfig){
        final SslContext sslContext = SSL_FACTORY.createSecureClientContext(trustStoreKeys);
        final ConnectionProvider connectionProvider = createConnectionProvider(connectionPoolConfig);
        return HttpClient.create(connectionProvider).secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
    }

    static HttpClient createInsecure() {
        final SslContext context = SSL_FACTORY.createInsecureClientContext();
        return HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(context));
    }

    static HttpClient createInsecure(ConnectionPoolConfig connectionPoolConfig) {
        final SslContext context = SSL_FACTORY.createInsecureClientContext();
        final ConnectionProvider connectionProvider = createConnectionProvider(connectionPoolConfig);
        return HttpClient.create(connectionProvider).secure(sslContextSpec -> sslContextSpec.sslContext(context));
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
