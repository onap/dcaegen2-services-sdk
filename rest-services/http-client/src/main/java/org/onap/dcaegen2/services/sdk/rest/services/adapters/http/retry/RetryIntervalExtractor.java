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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vavr.Tuple;
import io.vavr.Value;
import io.vavr.collection.Multimap;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;

import java.time.Duration;

class RetryIntervalExtractor {

    private static final String RETRY_AFTER_HEADER = HttpHeaderNames.RETRY_AFTER.toString();
    private static final int PAYLOAD_TOO_LARGE_HTTP_CODE = HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code();

    Option<Duration> extractDelay(HttpResponse response) {
        return response.statusCode() == PAYLOAD_TOO_LARGE_HTTP_CODE
                ? extractDelay(response.headers())
                : Option.none();
    }

    private Option<Duration> extractDelay(Multimap<String, String> headers) {
        return headers
                .map((key, value) -> Tuple.of(key.toLowerCase(), value))
                .get(RETRY_AFTER_HEADER)
                .toStream()
                .flatMap(Value::toStream)
                .map(this::parse)
                .find(d -> d >= 0)
                .map(Duration::ofSeconds);
    }

    private int parse(String str) {
        return Try.of(() -> Integer.parseInt(str)).getOrElse(-1);
    }
}
