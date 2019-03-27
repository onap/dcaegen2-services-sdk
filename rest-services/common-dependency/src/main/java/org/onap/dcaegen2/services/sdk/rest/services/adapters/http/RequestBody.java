/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
@Value.Immutable
public interface RequestBody {

    Publisher<ByteBuf> contents();

    @Nullable Integer length();

    static RequestBody chunkedFromString(Publisher<String> contents) {
        return chunkedFromString(contents, StandardCharsets.UTF_8);
    }

    static RequestBody chunkedFromString(Publisher<String> contents, Charset charset) {
        return ImmutableRequestBody.builder()
                .length(null)
                .contents(ByteBufFlux.fromString(contents, charset, ByteBufAllocator.DEFAULT))
                .build();
    }

    static RequestBody fromString(String contents) {
        return fromString(contents, StandardCharsets.UTF_8);
    }

    static RequestBody fromString(String contents, Charset charset) {
        ByteBuf encodedContents = ByteBufAllocator.DEFAULT.buffer();
        encodedContents.writeCharSequence(contents, charset);

        return ImmutableRequestBody.builder()
                .length(encodedContents.readableBytes())
                .contents(Mono.just(encodedContents.retain()))
                .build();
    }

    static RequestBody fromJson(JsonElement contents) {
        return fromString(contents.toString());
    }

}
