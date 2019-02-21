/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


public final class AaiHttpGetClient implements AaiHttpClient<String> {

    private HttpClient httpClient;
    private final AaiClientConfiguration configuration;


    public AaiHttpGetClient(AaiClientConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Mono<String> getAaiResponse(AaiModel aaiModel) {
        return httpClient
                .baseUrl(getUri(aaiModel.getCorrelationId()))
                .get()
                .responseContent()
                .aggregate()
                .asString();
    }

    public AaiHttpGetClient createAaiHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    String getUri(String pnfName) {
        return new URI.URIBuilder()
                .scheme(configuration.aaiProtocol())
                .host(configuration.aaiHost())
                .port(configuration.aaiPort())
                .path(configuration.aaiBasePath() + configuration.aaiPnfPath() + "/" + pnfName).build().toString();
    }
}
