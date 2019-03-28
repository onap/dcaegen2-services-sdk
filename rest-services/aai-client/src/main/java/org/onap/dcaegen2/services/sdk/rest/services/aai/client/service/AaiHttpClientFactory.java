/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service;

import io.netty.handler.ssl.SslContext;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiHttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiHttpClientFactory.class);

    private final AaiClientConfiguration configuration;
    private final SslFactory sslFactory;

    public AaiHttpClientFactory(AaiClientConfiguration configuration) {
        this(configuration, new SslFactory());
    }

    public AaiHttpClientFactory(AaiClientConfiguration configuration, SslFactory sslFactory) {
        this.configuration = configuration;
        this.sslFactory = sslFactory;
    }

    public CloudHttpClient build() {
        LOGGER.debug("Setting ssl context");
        return new CloudHttpClient(createSslContext());
    }

    private SslContext createSslContext() {
        if (configuration.enableAaiCertAuth()) {
            final SecurityKeys collectorSecurityKeys = ImmutableSecurityKeys.builder()
                    .keyStore(ImmutableSecurityKeysStore.of(resource(configuration.keyStorePath()).get()))
                    .keyStorePassword(Passwords.fromResource(configuration.keyStorePasswordPath()))
                    .trustStore(ImmutableSecurityKeysStore.of(resource(configuration.trustStorePath()).get()))
                    .trustStorePassword(Passwords.fromResource(configuration.trustStorePasswordPath()))
                    .build();
            return sslFactory.createSecureClientContext(collectorSecurityKeys);
        }
        return sslFactory.createInsecureClientContext();
    }

    private Try<Path> resource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()));
    }

    public static RequestDiagnosticContext createRequestDiagnosticContext() {
        return ImmutableRequestDiagnosticContext.builder()
                .invocationId(UUID.randomUUID()).requestId(UUID.randomUUID()).build();
    }

}
