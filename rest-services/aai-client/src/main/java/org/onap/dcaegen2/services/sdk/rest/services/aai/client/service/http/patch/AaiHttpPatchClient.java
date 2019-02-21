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

import io.netty.handler.codec.http.HttpHeaders;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

import java.util.UUID;
import java.util.function.Consumer;

import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_ONAP_REQUEST_ID;

public final class AaiHttpPatchClient {

    private HttpClient httpClient;
    private final AaiClientConfiguration configuration;
    private final JsonBodyBuilder jsonBodyBuilder;


    public AaiHttpPatchClient(final AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder) {
        this.configuration = configuration;
        this.jsonBodyBuilder = jsonBodyBuilder;
    }


    public Mono<Integer> getAaiProducerResponse(AaiModel aaiModel) {
        return patchAaiRequest(aaiModel);
    }

    public AaiHttpPatchClient createAaiHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }


    private Mono<Integer> patchAaiRequest(AaiModel aaiModel) {
        return httpClient
                .headers(addHeaders())
                .baseUrl(getUri(aaiModel.getCorrelationId()))
                .patch()
                .send(ByteBufFlux.fromString(Mono.just(jsonBodyBuilder.createJsonBody(aaiModel))))
                .responseSingle((res, content) -> Mono.just(res.status().code()));
    }


    String getUri(String pnfName) {
        return new URI.URIBuilder()
                .scheme(configuration.aaiProtocol())
                .host(configuration.aaiHost())
                .port(configuration.aaiPort())
                .path(configuration.aaiBasePath() + configuration.aaiPnfPath() + "/" + pnfName).build().toString();
    }

    private Consumer<? super HttpHeaders> addHeaders() {
        return h -> {
            h.add(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID));
            h.add(X_INVOCATION_ID, UUID.randomUUID().toString());
        };
    }
}
