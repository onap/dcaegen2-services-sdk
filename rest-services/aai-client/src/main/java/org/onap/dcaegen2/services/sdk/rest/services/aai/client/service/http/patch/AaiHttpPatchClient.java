/*-
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiHttpClientFactory.createRequestDiagnosticContext;

public final class AaiHttpPatchClient implements AaiHttpClient<AaiModel, HttpResponse> {

    private CloudHttpClient httpPatchClient;
    private final AaiClientConfiguration configuration;
    private final JsonBodyBuilder jsonBodyBuilder;


    public AaiHttpPatchClient(final AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder, CloudHttpClient httpPatchClient) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
        this.httpPatchClient = httpPatchClient;
    }

    public Mono<HttpResponse> getAaiResponse(AaiModel aaiModel) {
        return httpPatchClient
                .patch(getUri(aaiModel.getCorrelationId()), createRequestDiagnosticContext(), configuration.aaiHeaders(), jsonBodyBuilder, aaiModel);
    }

    private String getUri(String pnfName) {
        return new URI.URIBuilder()
                .scheme(configuration.aaiProtocol())
                .host(configuration.aaiHost())
                .port(configuration.aaiPort())
                .path(configuration.aaiBasePath() + configuration.aaiPnfPath() + "/" + pnfName).build().toString();
    }

}