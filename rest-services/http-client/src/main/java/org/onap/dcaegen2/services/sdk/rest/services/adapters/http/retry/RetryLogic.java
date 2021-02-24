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

import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.RetryableException;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;

public class RetryLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryLogic.class);

    private final RetryConfig retryConfig;
    private final RetryIntervalExtractor delayExtractor;

    public RetryLogic(RetryConfig retryConfig, RetryIntervalExtractor delayExtractor) {
        this.retryConfig = Objects.requireNonNull(retryConfig, "retryConfig must not be null");
        this.delayExtractor = Objects.requireNonNull(delayExtractor, "delayExtractor must not be null");
    }

    public Retry retry(RequestDiagnosticContext requestDiagnosticContext) {
        return Retry
                .max(retryConfig.retryCount())
                .doAfterRetryAsync(rc -> Mono.delay(calculateDelay(rc.failure())).then())
                .doBeforeRetry(retrySignal -> requestDiagnosticContext.withSlf4jMdc(
                        LOGGER.isTraceEnabled(), () -> LOGGER.trace("Retry: {}", retrySignal)))
                .filter(ex -> isRetryable(retryConfig, ex))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    }

    public boolean shouldRetry(int code) {
        return retryConfig.retryableHttpResponseCodes().contains(code);
    }

    private Duration calculateDelay(Throwable tx) {
        Duration retryInterval = retryConfig.retryInterval();
        if (tx instanceof RetryableException) {
            RetryableException ex = (RetryableException) tx;
            retryInterval = delayExtractor.extractDelay(ex.getResponse())
                    .getOrElse(retryInterval);
        }
        return retryInterval;
    }

    private boolean isRetryable(RetryConfig retryConfig, Throwable ex) {
        return retryConfig.retryableExceptions()
                .toStream()
                .exists(clazz -> clazz.isAssignableFrom(ex.getClass()));
    }
}
