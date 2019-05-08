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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import io.netty.handler.ssl.SslContext;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import reactor.netty.http.client.HttpClient;

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


    public static RxHttpClient create(SecurityKeys securityKeys) {
        final SslContext context = SSL_FACTORY.createSecureClientContext(securityKeys);
        return create(context);
    }

    public static RxHttpClient createInsecure() {
        final SslContext context = SSL_FACTORY.createInsecureClientContext();
        return create(context);
    }

    // TODO: make it private after removing CloudHttpClient
    static RxHttpClient create(@NotNull SslContext sslContext) {
        return new RxHttpClient(HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));
    }
}
