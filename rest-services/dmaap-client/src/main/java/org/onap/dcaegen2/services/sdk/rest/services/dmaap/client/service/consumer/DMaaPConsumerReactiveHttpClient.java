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

import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.sdk.rest.services.model.logging.MdcVariables.X_ONAP_REQUEST_ID;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI.URIBuilder;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/26/18
 */
public class DMaaPConsumerReactiveHttpClient {

    private final static String SLASH="/";
    private final String dmaapHostName;
    private final String dmaapProtocol;
    private final Integer dmaapPortNumber;
    private final String dmaapTopicName;
    private final String consumerGroup;
    private final String consumerId;
    private final String contentType;
    private final CloudHttpClient cloudHttpClient;

    /**
     * Constructor of DMaaPConsumerReactiveHttpClient.
     *
     * @param consumerConfiguration - DMaaP consumer configuration object
     */


    public DMaaPConsumerReactiveHttpClient(DmaapConsumerConfiguration consumerConfiguration, CloudHttpClient cloudHttpClient) {
        this.dmaapHostName = consumerConfiguration.dmaapHostName();
        this.dmaapProtocol = consumerConfiguration.dmaapProtocol();
        this.dmaapPortNumber = consumerConfiguration.dmaapPortNumber();
        this.dmaapTopicName = consumerConfiguration.dmaapTopicName();
        this.consumerGroup = consumerConfiguration.consumerGroup();
        this.consumerId = consumerConfiguration.consumerId();
        this.contentType = consumerConfiguration.dmaapContentType();
        this.cloudHttpClient = cloudHttpClient;
    }


    /**
     * Function for calling DMaaP HTTP consumer - consuming messages from Kafka/DMaaP from topic.
     *
     * @return reactive response from DMaaP in string format
     */
    public Mono<String> getDMaaPConsumerResponse() {
        RequestDiagnosticContext requestDiagnosticContext = ImmutableRequestDiagnosticContext.builder().invocationId(UUID.randomUUID()).contentType(contentType).requestId(UUID.randomUUID()).build();
        return cloudHttpClient.get(getUri().toString(),requestDiagnosticContext,String.class);
    }

    //TODO wyjasnic z Jaszczurem....
    private Consumer<HttpHeaders> getHeaders() {
        return httpHeaders -> {
            httpHeaders.set(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID));
            httpHeaders.set(X_INVOCATION_ID, UUID.randomUUID().toString());
            httpHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
        };
    }

    URI getUri() {
        return URI.create(
            new URIBuilder().scheme(dmaapProtocol).host(dmaapHostName).port(dmaapPortNumber).path(createRequestPath())
                .build().toString());
    }

    private String createRequestPath() {
        return  new StringBuilder().append(SLASH).append(dmaapTopicName).append(SLASH).append(consumerGroup).append(SLASH).append(consumerId).toString();
    }
}
