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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config;

import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import org.immutables.value.Value;

import java.net.ConnectException;

@Value.Immutable
public interface DmaapRetryConfig {

    Set<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = HashSet.of(ReadTimeoutException.class, ConnectException.class);
    RuntimeException ON_RETRY_EXHAUSTED_EXCEPTION = ReadTimeoutException.INSTANCE;
    Set<Integer> RETRYABLE_HTTP_CODES = HashSet.of(404, 408, 413, 429, 500, 502, 503, 504);

    @Value.Default
    default int retryCount() {
        return 3;
    }

    @Value.Default
    default int retryIntervalInSeconds() {
        return 1;
    }

    @Value.Check
    default void validate() {
        validateRetryCount();
        validateRetryInterval();
    }

    private void validateRetryCount() {
        int rc = retryCount();
        if (rc < 0)
            throw new IllegalArgumentException(String.format("Invalid value: %d, retryCount should be (0-n)", rc));
    }

    private void validateRetryInterval() {
        long ri = retryIntervalInSeconds();
        if (ri < 1)
            throw new IllegalArgumentException(String.format("Invalid value: %d, retryInterval should be (1-n)", ri));
    }
}
