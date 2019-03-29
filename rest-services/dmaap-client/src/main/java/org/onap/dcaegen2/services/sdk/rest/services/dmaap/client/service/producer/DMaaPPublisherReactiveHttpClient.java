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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer;


import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.DMaaPAbstractReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.DMaaPClientServiceUtils;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */
public class DMaaPPublisherReactiveHttpClient extends DMaaPAbstractReactiveHttpClient {

    private final DmaapPublisherConfiguration dmaapPublisherConfiguration;
    private final JsonBodyBuilder jsonBodyBuilder;
    private final CloudHttpClient cloudHttpClient;

    /**
     * Constructor DMaaPPublisherReactiveHttpClient.
     *
     * @param dmaapPublisherConfiguration - DMaaP producer configuration object
     * @param cloudHttpClient             - cloudHttpClient sending http requests
     */
    DMaaPPublisherReactiveHttpClient(DmaapPublisherConfiguration dmaapPublisherConfiguration,
                                     CloudHttpClient cloudHttpClient, JsonBodyBuilder jsonBodyBuilder) {
        this.dmaapPublisherConfiguration = dmaapPublisherConfiguration;
        this.cloudHttpClient = cloudHttpClient;
        this.jsonBodyBuilder = jsonBodyBuilder;
    }

    /**
     * Function for calling DMaaP HTTP producer - post request to DMaaP.
     *
     * @param dmaapModel - object which will be sent to DMaaP
     * @return status code of operation
     */

    public Mono<HttpResponse> getDMaaPProducerResponse(
            DmaapModel dmaapModel,
            Optional<RequestDiagnosticContext> requestDiagnosticContextOptional) {
        return Mono.defer(() -> {
            Map<String, String> headers = DMaaPClientServiceUtils.getHeaders(dmaapPublisherConfiguration.dmaapContentType());
            if (requestDiagnosticContextOptional.isPresent()) {
                cloudHttpClient
                        .post(getUri().toString(), requestDiagnosticContextOptional.get(), headers, jsonBodyBuilder,
                                dmaapModel);
            }
            return cloudHttpClient
                    .post(getUri().toString(), getRequestDiagnosticContext(), headers, jsonBodyBuilder, dmaapModel);
        });
    }


    URI getUri() {
        return URI.create(
                new URIBuilder().scheme(dmaapPublisherConfiguration.dmaapProtocol())
                        .host(dmaapPublisherConfiguration.dmaapHostName()).port(dmaapPublisherConfiguration.dmaapPortNumber())
                        .path(createRequestPath())
                        .build().toString());
    }

    private String createRequestPath() {
        return new StringBuilder().append(SLASH).append(dmaapPublisherConfiguration.dmaapTopicName()).toString();
    }

}
