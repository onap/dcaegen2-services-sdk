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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DmaapRetryConfigTest {
    @Test
    void shouldSuccessfullyCreateObject() {
        DmaapRetryConfig retryConfig = ImmutableDmaapRetryConfig.builder()
                .retryIntervalInSeconds(1)
                .retryCount(0)
                .build();

        assertThat(retryConfig.retryIntervalInSeconds()).isOne();
        assertThat(retryConfig.retryCount()).isZero();
    }

    @Test
    void shouldSuccessfullyCreateObjectForDefaults() {
        DmaapRetryConfig retryConfig = ImmutableDmaapRetryConfig.builder().build();

        assertThat(retryConfig.retryIntervalInSeconds()).isOne();
        assertThat(retryConfig.retryCount()).isEqualTo(3);
    }

    @Test
    void shouldThrowInvalidArgumentExceptionForInvalidRetryInterval() {
        assertThrows(IllegalArgumentException.class, () -> withRetryInterval(0));
        assertThrows(IllegalArgumentException.class, () -> withRetryInterval(-3));
    }

    @Test
    void shouldThrowInvalidArgumentExceptionForInvalidRetryCount() {
        assertThrows(IllegalArgumentException.class, () -> withRetryCount(-1));
        assertThrows(IllegalArgumentException.class, () -> withRetryCount(-3));
    }

    private void withRetryInterval(int ri) {
        ImmutableDmaapRetryConfig.builder()
                .retryIntervalInSeconds(ri)
                .build();
    }

    private void withRetryCount(int rc) {
        ImmutableDmaapRetryConfig.builder()
                .retryCount(rc)
                .build();
    }
}
