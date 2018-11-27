/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

public class DmaaPRestTemplateFactory {

    /**
     * Function for creating RestTemplate object.
     *
     * @param publisherConfiguration - DMaaP publisher configuration object
     * @return RestTemplate with correct ssl configuration
     */
    public Mono<RestTemplate> build(DmaapPublisherConfiguration publisherConfiguration) {
        if (publisherConfiguration.enableDmaapCertAuth()) {
            return createRestTemplateWithSslSetup(publisherConfiguration);
        }

        return Mono.just(new RestTemplate());
    }

    private Mono<RestTemplate> createRestTemplateWithSslSetup(DmaapPublisherConfiguration publisherConfiguration) {
        try {
            RestTemplateBuilder builder = new RestTemplateBuilder();

            SSLContext sslContext = createSslContext(publisherConfiguration,
                    loadPasswordFromFile(publisherConfiguration.keyStorePasswordPath()),
                    loadPasswordFromFile(publisherConfiguration.trustStorePasswordPath()));

            return Mono.just(builder
                    .requestFactory(() -> createRequestFactory(sslContext)).build());

        } catch (GeneralSecurityException | IOException e) {
            return Mono.error(e);
        }
    }

    private SSLContext createSslContext(DmaapPublisherConfiguration publisherConfiguration,
                                        String keyStorePassword, String trustStorePassword)
            throws IOException, GeneralSecurityException {
        return new SSLContextBuilder()
                        .loadKeyMaterial(
                                keyStore(publisherConfiguration.keyStorePath(), keyStorePassword),
                                keyStorePassword.toCharArray())
                        .loadTrustMaterial(
                                getFile(publisherConfiguration.trustStorePath()), trustStorePassword.toCharArray())
                        .build();
    }

    private HttpComponentsClientHttpRequestFactory createRequestFactory(SSLContext sslContext) {
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory).build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private KeyStore keyStore(String keyStoreFile, String keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(getResource(keyStoreFile), keyStorePassword.toCharArray());
        return ks;
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    private InputStream getResource(String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    private String loadPasswordFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

}
