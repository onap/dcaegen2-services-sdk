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
package org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions;

import java.io.IOException;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class HttpException extends IOException {

    private final String url;
    private final int responseCode;
    private final String reason;

    public HttpException(String url, int responseCode, String reason) {
        this.url = url;
        this.responseCode = responseCode;
        this.reason = reason;
    }

    @Override
    public String getMessage() {
        return String.format("Request failed for URL '%s'. Response code: %d %s",
                url,
                responseCode,
                reason);
    }
}
