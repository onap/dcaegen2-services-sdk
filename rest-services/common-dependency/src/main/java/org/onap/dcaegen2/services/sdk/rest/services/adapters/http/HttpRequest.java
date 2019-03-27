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
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.function.BiFunction;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
@Value.Immutable
public interface HttpRequest {

    String url();

    HttpMethod method();

    @Value.Default
    default boolean chunkedTransferEncoding() {
        return false;
    }

    @Value.Default
    default RequestDiagnosticContext diagnosticContext() {
        return RequestDiagnosticContext.create();
    }

    @Value.Default
    default Map<String, String> customHeaders() {
        return HashMap.empty();
    }

    @Value.Default
    default Publisher<ByteBuf> body() {
        return Mono.empty();
    }

    @Value.Derived
    default Map<String, String> headers() {
        final RequestDiagnosticContext ctx = diagnosticContext();
        return ctx == null
                ? customHeaders()
                : customHeaders().merge(ctx.remoteCallHttpHeaders());
    }
}
