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
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;


import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_ONAP_REQUEST_ID;


@Deprecated
public class AaiReactiveHttpPatchClient {

    private WebClient webClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiBasePath;
    private final String aaiPnfPath;
    private final JsonBodyBuilder jsonBodyBuilder;

    /**
     * Constructor of AaiProducerReactiveHttpClient.
     *
     * @param configuration - AAI producer configuration object
     */
    public AaiReactiveHttpPatchClient(AaiClientConfiguration configuration, JsonBodyBuilder jsonBodyBuilder) {
        this.aaiHost = configuration.aaiHost();
        this.aaiProtocol = configuration.aaiProtocol();
        this.aaiHostPortNumber = configuration.aaiPort();
        this.aaiBasePath = configuration.aaiBasePath();
        this.aaiPnfPath = configuration.aaiPnfPath();
        this.jsonBodyBuilder = jsonBodyBuilder;
    }

    /**
     * Function for calling AAI Http producer - patch request to AAI database.
     *
     * @param aaiModel - object which will be sent to AAI database
     * @return status code of operation
     */
    public Mono<ClientResponse> getAaiProducerResponse(AaiModel aaiModel) {
        return patchAaiRequest(aaiModel);
    }

    public AaiReactiveHttpPatchClient createAaiWebClient(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    private Mono<ClientResponse> patchAaiRequest(AaiModel aaiModel) {
        return
            webClient.patch()
                .uri(getUri(aaiModel.getCorrelationId()))
                .header(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID))
                .header(X_INVOCATION_ID, UUID.randomUUID().toString())
                .body(Mono.just(jsonBodyBuilder.createJsonBody(aaiModel)), String.class)
                .exchange();
    }

    URI getUri(String pnfName) {
        return new DefaultUriBuilderFactory().builder().scheme(aaiProtocol).host(aaiHost).port(aaiHostPortNumber)
            .path(aaiBasePath + aaiPnfPath + "/" + pnfName).build();
    }
}