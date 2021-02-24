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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http.retry;

import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.HashMultimap;
import io.vavr.collection.HashSet;
import io.vavr.control.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ImmutableRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.RetryableException;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetryLogicTest {

    private static final HashSet<Integer> RETRYABLE_HTTP_RESPONSE_CODES =
            HashSet.of(404, 408, 413, 429, 500, 502, 503, 504);
    private static final HashSet<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS =
            HashSet.of(ReadTimeoutException.class, ConnectException.class);
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(5);
    private static final int RETRY_COUNT = 3;
    private static final Duration RETRY_EXHAUSTED = Duration.ofSeconds(RETRY_COUNT * RETRY_INTERVAL.getSeconds());
    private static final RetryConfig RETRY_CONFIG = ImmutableRetryConfig.builder()
            .retryCount(RETRY_COUNT)
            .retryInterval(RETRY_INTERVAL)
            .retryableHttpResponseCodes(RETRYABLE_HTTP_RESPONSE_CODES)
            .customRetryableExceptions(RETRYABLE_EXCEPTIONS)
            .build();

    private final RequestDiagnosticContext dummyContext = mock(RequestDiagnosticContext.class);
    private final RetryIntervalExtractor retryIntervalExtractor = mock(RetryIntervalExtractor.class);
    private RetryLogic retryLogic;

    @BeforeEach
    void setUp() {
        retryLogic = new RetryLogic(RETRY_CONFIG, retryIntervalExtractor);
    }

    @Test
    void shouldRetryWhenRetryableException() {
        // when
        Mono<?> mono = Mono
                .error(ReadTimeoutException.INSTANCE)
                .retryWhen(retryLogic.retry(dummyContext));

        // then
        StepVerifier.withVirtualTime(() -> mono)
                .expectSubscription()
                .expectNoEvent(RETRY_EXHAUSTED)
                .expectError(ReadTimeoutException.class)
                .verify();
    }

    @Test
    void shouldNotRetryWhenUnretryableException() {
        // when
        Mono<?> mono = Mono
                .error(RuntimeException::new)
                .retryWhen(retryLogic.retry(dummyContext));

        // then
        StepVerifier.withVirtualTime(() -> mono)
                .expectSubscription()
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldUseRetryIntervalFromExtractorWhenRetryableStatusCode() {
        // given
        HttpResponse httpResponse = httpResponse413();
        Duration retryInterval = Duration.ofSeconds(10);
        when(retryIntervalExtractor.extractDelay(httpResponse))
                .thenReturn(Option.of(retryInterval));

        // when
        Mono<?> mono = Mono
                .error(() -> new RetryableException(httpResponse))
                .retryWhen(retryLogic.retry(dummyContext));

        // then
        long noEvents = RETRY_COUNT * retryInterval.getSeconds();
        StepVerifier.withVirtualTime(() -> mono)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(noEvents))
                .expectError(RetryableException.class)
                .verify();
        verify(retryIntervalExtractor, times(RETRY_COUNT)).extractDelay(httpResponse);
    }

    private ImmutableHttpResponse httpResponse413() {
        return ImmutableHttpResponse.builder()
                .url("")
                .statusCode(413)
                .rawBody("".getBytes())
                .headers(HashMultimap.withSeq().empty())
                .build();
    }

}
