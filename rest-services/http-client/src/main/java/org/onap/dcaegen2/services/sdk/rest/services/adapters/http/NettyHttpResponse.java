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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class NettyHttpResponse implements HttpResponse {

    private final String url;
    private final HttpResponseStatus status;
    private final byte[] body;

    NettyHttpResponse(String url, HttpResponseStatus status, byte[] body) {
        this.url = url;
        this.status = status;
        this.body = body;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public boolean successful() {
        return status.codeClass() == HttpStatusClass.SUCCESS;
    }

    @Override
    public int statusCode() {
        return status.code();
    }

    @Override
    public String statusReason() {
        return status.reasonPhrase();
    }

    @Override
    public byte[] rawBody() {
        return new byte[0];
    }

    @Override
    public String bodyAsString(Charset charset) {
        return new String(body, charset);
    }

}
