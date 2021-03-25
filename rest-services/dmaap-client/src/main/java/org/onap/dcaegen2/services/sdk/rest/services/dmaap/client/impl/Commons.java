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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since April 2019
 */
final class Commons {

    private Commons() {
    }

    static String extractFailReason(HttpResponse httpResponse) {
        return String.format("%d %s%n%s", httpResponse.statusCode(), httpResponse.statusReason(),
                httpResponse.bodyAsString());
    }

    static Tuple2<String, String> basicAuthHeader(AafCredentials credentials) {
        Objects.requireNonNull(credentials, "aafCredentials");
        String basicAuthFormat = basicAuthFormat(credentials);
        byte[] utf8 = bytesUTF8(basicAuthFormat);
        String userCredentials = Base64.getEncoder().encodeToString(utf8);
        return Tuple.of("Authorization", "Basic " + userCredentials);
    }

    private static String basicAuthFormat(AafCredentials credentials) {
        String username = getOrEmpty(credentials.username());
        String password = getOrEmpty(credentials.password());
        return username.concat(":").concat(password);
    }

    private static String getOrEmpty(String text) {
        return Option.of(text).getOrElse("");
    }

    private static byte[] bytesUTF8(String text) {
        return Option.of(text)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .getOrElse(new byte[0]);
    }
}
