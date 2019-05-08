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

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public enum HttpMethod {

    CONNECT(io.netty.handler.codec.http.HttpMethod.CONNECT),
    DELETE(io.netty.handler.codec.http.HttpMethod.DELETE),
    GET(io.netty.handler.codec.http.HttpMethod.GET),
    HEAD(io.netty.handler.codec.http.HttpMethod.HEAD),
    OPTIONS(io.netty.handler.codec.http.HttpMethod.OPTIONS),
    POST(io.netty.handler.codec.http.HttpMethod.POST),
    PATCH(io.netty.handler.codec.http.HttpMethod.PATCH),
    PUT(io.netty.handler.codec.http.HttpMethod.PUT),
    TRACE(io.netty.handler.codec.http.HttpMethod.TRACE);

    private final io.netty.handler.codec.http.HttpMethod nettyMethod;

    HttpMethod(io.netty.handler.codec.http.HttpMethod nettyMethod) {
        this.nettyMethod = nettyMethod;
    }

    io.netty.handler.codec.http.HttpMethod asNetty() {
        return nettyMethod;
    }
}
