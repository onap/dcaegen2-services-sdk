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

import io.vavr.Tuple;
import io.vavr.collection.HashMultimap;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DelayExtractorTest {

    private static final HttpResponse DEFAULT = ImmutableHttpResponse.builder()
            .url("")
            .statusCode(0)
            .rawBody("".getBytes())
            .headers(HashMultimap.withSeq().empty())
            .build();

    private static final RetryIntervalExtractor DELAY_EXTRACTOR = new RetryIntervalExtractor();

    @Test
    void shouldExtractValueFromFirstValidHeaderWhenStatusCode413() {
        // given
        HttpResponse response = ImmutableHttpResponse.copyOf(DEFAULT)
                .withStatusCode(413)
                .withHeaders(HashMultimap.withSeq().ofEntries(
                        Tuple.of("Any", "12"),
                        Tuple.of("Retry-After", "15"),
                        Tuple.of("Retry-After", "100")
                ));

        // when
        Option<Duration> delay = DELAY_EXTRACTOR.extractDelay(response);

        // then
        assertThat(delay.get()).isEqualTo(Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 400, 500})
    void shouldExtractNoValueWhenStatusCodeDifferentThan413(int statusCode) {
        // given
        HttpResponse response = ImmutableHttpResponse.copyOf(DEFAULT)
                .withStatusCode(statusCode);

        // when
        Option<Duration> delay = DELAY_EXTRACTOR.extractDelay(response);

        // then
        assertThat(delay).isEqualTo(Option.none());
    }

    @ParameterizedTest
    @CsvSource({
            "Retry-After,",
            "Retry-After,invalid",
            "Retry-After,999999999999",
            "Any,12"})
    void shouldExtractNoValueWhenStatusCode413AndNoValidHeader(String key, String value) {
        // given
        HttpResponse response = ImmutableHttpResponse.copyOf(DEFAULT)
                .withStatusCode(413)
                .withHeaders(HashMultimap.withSeq().ofEntries(Tuple.of(key, value)));

        // when
        Option<Duration> delay = DELAY_EXTRACTOR.extractDelay(response);

        // then
        assertThat(delay).isEqualTo(Option.none());
    }
}
