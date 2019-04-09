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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer;

import io.netty.handler.ssl.SslContext;
import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;

public class DmaaPRestTemplateFactory {

    private SslFactory sslFactory;

    public DmaaPRestTemplateFactory() {
        this(new SslFactory());
    }

    DmaaPRestTemplateFactory(SslFactory sslFactory) {
        this.sslFactory = sslFactory;
    }

    /**
     * Function for creating RestTemplate object.
     *
     * @param publisherConfiguration - DMaaP publisher configuration object
     * @return RestTemplate with correct ssl configuration
     */
    public CloudHttpClient build(DmaapPublisherConfiguration publisherConfiguration){
        SslContext sslContext = createSslContext(publisherConfiguration);
        return new CloudHttpClient(sslContext);
    }

    private SslContext createSslContext(DmaapPublisherConfiguration consumerConfiguration){
        if (consumerConfiguration.enableDmaapCertAuth()) {
            final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
                    .keyStore(ImmutableSecurityKeysStore.of(resource(consumerConfiguration.keyStorePath()).get()))
                    .keyStorePassword(Passwords.fromResource(consumerConfiguration.keyStorePasswordPath()))
                    .trustStore(ImmutableSecurityKeysStore.of(resource(consumerConfiguration.trustStorePath()).get()))
                    .trustStorePassword(Passwords.fromResource(consumerConfiguration.trustStorePasswordPath()))
                    .build();
            return sslFactory.createSecureClientContext(securityKeys);
        }
        return sslFactory.createInsecureClientContext();
    }

    private Try<Path> resource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()));
    }
}
