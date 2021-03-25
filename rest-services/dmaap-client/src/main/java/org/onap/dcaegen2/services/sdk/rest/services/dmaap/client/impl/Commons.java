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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        Charset utf8 = StandardCharsets.UTF_8;
        byte[] username = toBytes(credentials.username(), utf8);
        byte[] separator = ":".getBytes(utf8);
        byte[] password = toBytes(credentials.password(), utf8);
        byte[] combined = addAll(concat(username, separator, password));
        String userCredentials = Base64.getEncoder().encodeToString(combined);
        return Tuple.of("Authorization", "Basic " + userCredentials);
    }

    private static byte[] toBytes(String text, Charset charset) {
        return Option.of(text)
                .map(s -> s.getBytes(charset))
                .getOrElse(new byte[0]);
    }

    private static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return array2.clone();
        } else if (array2 == null) {
            return array1.clone();
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

}
