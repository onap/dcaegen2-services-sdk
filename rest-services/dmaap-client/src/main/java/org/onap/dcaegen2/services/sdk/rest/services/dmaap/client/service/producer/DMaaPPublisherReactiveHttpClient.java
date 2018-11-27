/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

import static org.onap.dcaegen2.services.prh.model.CommonFunctions.createJsonBody;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.X_ONAP_REQUEST_ID;

import java.net.URI;
import java.util.UUID;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */
public class DMaaPPublisherReactiveHttpClient {

    private final Logger logger = LoggerFactory.getLogger(DMaaPPublisherReactiveHttpClient.class);
    private final String dmaapHostName;
    private final Integer dmaapPortNumber;
    private final String dmaapProtocol;
    private final String dmaapTopicName;
    private final String dmaapContentType;
    private final Mono<RestTemplate> restTemplateMono;

    /**
     * Constructor DMaaPPublisherReactiveHttpClient.
     *
     * @param dmaapPublisherConfiguration - DMaaP producer configuration object
     */
    DMaaPPublisherReactiveHttpClient(DmaapPublisherConfiguration dmaapPublisherConfiguration,
                                     Mono<RestTemplate> restTemplateMono) {
        this.dmaapHostName = dmaapPublisherConfiguration.dmaapHostName();
        this.dmaapProtocol = dmaapPublisherConfiguration.dmaapProtocol();
        this.dmaapPortNumber = dmaapPublisherConfiguration.dmaapPortNumber();
        this.dmaapTopicName = dmaapPublisherConfiguration.dmaapTopicName();
        this.dmaapContentType = dmaapPublisherConfiguration.dmaapContentType();
        this.restTemplateMono = restTemplateMono;
    }

    /**
     * Function for calling DMaaP HTTP producer - post request to DMaaP.
     *
     * @param consumerDmaapModelMono - object which will be sent to DMaaP
     * @return status code of operation
     */

    public Mono<ResponseEntity<String>> getDMaaPProducerResponse(ConsumerDmaapModel consumerDmaapModelMono) {
        return Mono.defer(() -> {
            HttpEntity<String> request = new HttpEntity<>(createJsonBody(consumerDmaapModelMono), getAllHeaders());
            logger.info("Request: {} {}", getUri(), request);
            return restTemplateMono.map(
                restTemplate -> restTemplate.exchange(getUri(), HttpMethod.POST, request, String.class));
        });
    }

    private HttpHeaders getAllHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID));
        headers.set(X_INVOCATION_ID, UUID.randomUUID().toString());
        headers.set(HttpHeaders.CONTENT_TYPE, dmaapContentType);
        return headers;

    }

    URI getUri() {
        return new DefaultUriBuilderFactory().builder().scheme(dmaapProtocol).host(dmaapHostName).port(dmaapPortNumber)
                .path(dmaapTopicName).build();
    }

}
