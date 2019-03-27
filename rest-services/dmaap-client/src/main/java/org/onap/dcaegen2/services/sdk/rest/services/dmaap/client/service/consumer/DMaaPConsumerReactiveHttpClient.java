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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer;

import com.google.gson.JsonElement;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.DMaaPAbstractReactiveHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.DMaaPClientServiceUtils;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/26/18
 */
public class DMaaPConsumerReactiveHttpClient extends DMaaPAbstractReactiveHttpClient {

    private final DmaapConsumerConfiguration consumerConfiguration;
    private final CloudHttpClient cloudHttpClient;

    /**
     * Constructor of DMaaPConsumerReactiveHttpClient.
     *
     * @param consumerConfiguration - DMaaP consumer configuration object
     */

    public DMaaPConsumerReactiveHttpClient(DmaapConsumerConfiguration consumerConfiguration,
        CloudHttpClient cloudHttpClient) {
        this.consumerConfiguration = consumerConfiguration;
        this.cloudHttpClient = cloudHttpClient;
    }

    /**
     * Function for calling DMaaP HTTP consumer - consuming messages from Kafka/DMaaP from topic.
     *
     * @return reactive response from DMaaP in string format
     */
    public Mono<JsonElement> getDMaaPConsumerResponse(
        Optional<RequestDiagnosticContext> requestDiagnosticContextOptional) {
        Map<String, String> headers = DMaaPClientServiceUtils.getHeaders(consumerConfiguration.dmaapContentType());
        if (requestDiagnosticContextOptional.isPresent()) {
            return cloudHttpClient
                .get(getUri().toString(), requestDiagnosticContextOptional.get(), headers, JsonElement.class);
        }
        RequestDiagnosticContext requestDiagnosticContext = ImmutableRequestDiagnosticContext.builder()
            .invocationId(UUID.randomUUID()).requestId(UUID.randomUUID()).build();
        return cloudHttpClient.get(getUri().toString(), requestDiagnosticContext, headers, JsonElement.class);
    }

    URI getUri() {
        return URI.create(
            new URIBuilder().scheme(consumerConfiguration.dmaapProtocol()).host(consumerConfiguration.dmaapHostName())
                .port(consumerConfiguration.dmaapPortNumber()).path(createRequestPath())
                .build().toString());
    }

    private String createRequestPath() {
        return new StringBuilder().append(SLASH).append(consumerConfiguration.dmaapTopicName()).append(SLASH)
            .append(consumerConfiguration.consumerGroup())
            .append(SLASH).append(consumerConfiguration.consumerId()).toString();
    }
}
