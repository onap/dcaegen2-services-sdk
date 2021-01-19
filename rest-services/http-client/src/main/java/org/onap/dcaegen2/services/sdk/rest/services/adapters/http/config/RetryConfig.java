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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

@Value.Immutable
public interface RetryConfig {

    int retryCount();

    Duration retryInterval();

    @Value.Default
    default Set<Integer> retryableHttpResponseCodes() {
        return HashSet.empty();
    }

    @Value.Default
    default Set<Class<? extends Throwable>> customRetryableExceptions() {
        return HashSet.empty();
    }

    @Value.Derived
    default Set<Class<? extends Throwable>> retryableExceptions() {
        Set<Class<? extends Throwable>> result = customRetryableExceptions();
        if (retryableHttpResponseCodes().nonEmpty()) {
            result = result.add(RetryableException.class);
        }
        return result;
    }

    @Nullable RuntimeException onRetryExhaustedException();

    class RetryableException extends RuntimeException {}
}
