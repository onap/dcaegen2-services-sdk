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

import com.google.common.primitives.Bytes;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.lang3.ArrayUtils;
import org.immutables.value.Value;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Value.Immutable
public interface SecureTopicCredentials {

    String username();

    char[] password();

    @Value.Derived
    default Tuple2<String, String> basicAuthHeader() {
        Charset utf8 = StandardCharsets.UTF_8;
        byte[] username = username().getBytes(utf8);
        byte[] separator = ":".getBytes(utf8);
        byte[] password = toBytes(password(), utf8);
        byte[] combined = ArrayUtils.addAll(Bytes.concat(username, separator, password));
        String userCredentials = Base64.getEncoder().encodeToString(combined);
        return Tuple.of("Authorization", "Basic " + userCredentials);
    }

    private byte[] toBytes(char[] chars, Charset charset) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }
}
