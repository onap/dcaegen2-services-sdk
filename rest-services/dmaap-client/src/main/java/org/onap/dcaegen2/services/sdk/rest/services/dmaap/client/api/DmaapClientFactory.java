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
package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ImmutableRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ImmutableRxHttpClientConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RxHttpClientConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasonPresenter;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterPublisherImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterSubscriberImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;

import java.time.Duration;

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapRetryConfig.ON_RETRY_EXHAUSTED_EXCEPTION;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapRetryConfig.RETRYABLE_EXCEPTIONS;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapRetryConfig.RETRYABLE_HTTP_CODES;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class DmaapClientFactory {

    private DmaapClientFactory() {
    }

    public static @NotNull MessageRouterPublisher createMessageRouterPublisher(
            @NotNull MessageRouterPublisherConfig clientConfiguration) {

        return new MessageRouterPublisherImpl(
                createHttpClient(clientConfiguration),
                clientConfiguration.maxBatchSize(),
                clientConfiguration.maxBatchDuration(),
                new ClientErrorReasonPresenter());
    }

    public static @NotNull MessageRouterSubscriber createMessageRouterSubscriber(
            @NotNull MessageRouterSubscriberConfig clientConfiguration) {
        return new MessageRouterSubscriberImpl(
                createHttpClient(clientConfiguration),
                clientConfiguration.gsonInstance(),
                new ClientErrorReasonPresenter());
    }

    private static @NotNull RxHttpClient createHttpClient(DmaapClientConfiguration config) {
        RxHttpClientConfig clientConfig = ImmutableRxHttpClientConfig.builder()
                .retryConfig(createRetry(config))
                .build();
        return config.securityKeys() == null
                ? RxHttpClientFactory.create(clientConfig)
                : RxHttpClientFactory.create(config.securityKeys(), clientConfig);
    }

    private static RetryConfig createRetry(DmaapClientConfiguration config) {
        return Option.of(config.retryConfig())
                .map(rc -> ImmutableRetryConfig.builder()
                        .retryInterval(Duration.ofSeconds(rc.retryIntervalInSeconds()))
                        .retryCount(rc.retryCount())
                        .retryableHttpResponseCodes(RETRYABLE_HTTP_CODES)
                        .customRetryableExceptions(RETRYABLE_EXCEPTIONS)
                        .onRetryExhaustedException(ON_RETRY_EXHAUSTED_EXCEPTION)
                        .build())
                .getOrNull();
    }
}

