/*-
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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


package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.put;

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiHttpClientFactory.createRequestDiagnosticContext;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import reactor.core.publisher.Mono;

public class AaiHttpPutClient implements AaiHttpClient<AaiModel, HttpResponse> {

    private CloudHttpClient httpPutClient;
    private final AaiClientConfiguration configuration;
    private final JsonBodyBuilder jsonBodyBuilder;
    private final String uri;

    public AaiHttpPutClient(final AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder, String uri,
            CloudHttpClient httpPutClient) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
        this.uri = uri;
        this.httpPutClient = httpPutClient;
    }

    @Override
    public Mono<HttpResponse> getAaiResponse(AaiModel aaiModel) {
        return httpPutClient
                .put(uri, createRequestDiagnosticContext(), configuration.aaiHeaders(), jsonBodyBuilder, aaiModel);
    }
}
