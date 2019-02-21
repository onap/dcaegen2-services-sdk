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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.ssl.SslContext;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.ssl.SslFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import javax.net.ssl.SSLException;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiConsumer;

public class AaiHttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiHttpClientFactory.class);

    private final String aaiUserName;
    private final String aaiUserPassword;
    private final Map<String, String> aaiHeaders;
    private final Boolean enableAaiCertAuth;
    private final String trustStorePath;
    private final String trustStorePasswordPath;
    private final String keyStorePath;
    private final String keyStorePasswordPath;
    private final SslFactory sslFactory;


    public AaiHttpClientFactory(SslFactory sslFactory, AaiClientConfiguration configuration) {
        this.aaiUserName = configuration.aaiUserName();
        this.aaiUserPassword = configuration.aaiUserPassword();
        this.aaiHeaders = configuration.aaiHeaders();
        this.trustStorePath = configuration.trustStorePath();
        this.trustStorePasswordPath = configuration.trustStorePasswordPath();
        this.keyStorePath = configuration.keyStorePath();
        this.keyStorePasswordPath = configuration.keyStorePasswordPath();
        this.enableAaiCertAuth = configuration.enableAaiCertAuth();
        this.sslFactory = sslFactory;
    }

    public HttpClient build() throws SSLException {
        LOGGER.debug("Setting ssl context");

        SslContext sslContext = createSslContext();

        return HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
                .headers(this::settingHeaders)
                .doOnRequest(logRequest())
                .doOnResponse(logResponse());
    }


    private SslContext createSslContext() throws SSLException {
        if (enableAaiCertAuth) {
            return sslFactory.createSecureContext(
                    keyStorePath, keyStorePasswordPath, trustStorePath, trustStorePasswordPath
            );
        }
        return sslFactory.createInsecureContext();
    }

    private HttpHeaders settingHeaders(HttpHeaders httpHeaders) {
        httpHeaders.add("Authorization", "Basic " + performBasicAuthentication());
        for(Map.Entry<String,String> header : aaiHeaders.entrySet())
            httpHeaders.add(header.getKey(), header.getValue());
        return httpHeaders;
    }

    private String performBasicAuthentication() {
        return Base64.getEncoder().encodeToString((aaiUserName + ":" + aaiUserPassword).getBytes());
    }

    private static BiConsumer<HttpClientRequest, Connection> logRequest() {
        return (httpClientRequest, connection) -> {
            LOGGER.info("Request: {} {}", httpClientRequest.method(), httpClientRequest.uri());
            httpClientRequest.requestHeaders().forEach(stringStringEntry ->
                    LOGGER.info("{}={}", stringStringEntry.getKey(), stringStringEntry.getValue())
            );
        };
    }

    private static BiConsumer<? super HttpClientResponse, ? super Connection>  logResponse() {
        return (httpClientResponse, connection) ->
                LOGGER.info("ResponseStatus {}", httpClientResponse.status().code());
    }

}
