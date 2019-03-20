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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public final class RequestBody {

    private RequestBody() {
    }

    public static Publisher<ByteBuf> fromString(String contents) {
        return fromString(contents, StandardCharsets.UTF_8);
    }

    public static Publisher<ByteBuf> fromString(String contents, Charset charset) {
        return ByteBufFlux.fromString(Mono.just(contents), charset, ByteBufAllocator.DEFAULT);
    }

    public static Publisher<ByteBuf> fromJson(JsonElement contents) {
        return fromString(contents.toString());
    }

}
