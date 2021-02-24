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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions;

import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;

import java.util.Objects;

public class RetryableException extends RuntimeException {

    private final HttpResponse response;

    public RetryableException(HttpResponse response) {
        this.response = Objects.requireNonNull(response, "response must not be null");
    }

    public HttpResponse getResponse() {
        return response;
    }
}
