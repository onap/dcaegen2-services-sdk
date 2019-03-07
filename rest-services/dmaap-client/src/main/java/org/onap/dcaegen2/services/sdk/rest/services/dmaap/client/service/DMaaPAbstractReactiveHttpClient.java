/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;

public abstract class DMaaPAbstractReactiveHttpClient {

    protected final static String CONTENT_TYPE = "Content-Type";
    protected final static String SLASH = "/";

    protected RequestDiagnosticContext getRequestDiagnosticContext() {
        return ImmutableRequestDiagnosticContext.builder()
            .invocationId(UUID.randomUUID()).requestId(UUID.randomUUID()).build();
    }

    protected Map<String, String> getHeaders(String contentType) {
        Map<String, String> header = Collections.emptyMap();
        header.put(CONTENT_TYPE, contentType);
        return header;
    }

}
