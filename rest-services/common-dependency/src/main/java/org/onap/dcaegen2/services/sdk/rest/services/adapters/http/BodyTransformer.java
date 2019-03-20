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

import io.netty.buffer.ByteBuf;
import io.vavr.Function1;
import java.nio.charset.Charset;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public interface BodyTransformer<T> extends Function1<Mono<ByteBuf>, Mono<T>> {

    static <T> BodyTransformer<T> lift(Function1<ByteBuf, T> mapper) {
        return body -> body.map(mapper);
    }


    static BodyTransformer<String> asString(Charset charset) {
        return body -> body
                .map(bb -> bb.readCharSequence(bb.readableBytes(), charset).toString());
    }
}
