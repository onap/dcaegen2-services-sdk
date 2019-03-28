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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.apache.commons.text.StringSubstitutor;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiHttpClientFactory.createRequestDiagnosticContext;

public class AaiGetServiceInstanceClient implements
        AaiHttpClient<AaiServiceInstanceQueryModel, HttpResponse> {

    //variables for query "/business/customers/customer/${customer}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${serviceInstanceId}"
    public static final String CUSTOMER = "customer";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    private CloudHttpClient httpGetClient;
    private final AaiClientConfiguration configuration;

    public AaiGetServiceInstanceClient(final AaiClientConfiguration configuration, final CloudHttpClient httpGetClient) {
        this.configuration = configuration;
        this.httpGetClient = httpGetClient;
    }

    @Override
    public Mono<HttpResponse> getAaiResponse(AaiServiceInstanceQueryModel aaiModel) {
        final Map<String, String> mapping = HashMap.of(
                CUSTOMER, aaiModel.customerId(),
                SERVICE_TYPE, aaiModel.serviceType(),
                SERVICE_INSTANCE_ID, aaiModel.serviceInstanceId());

        final StringSubstitutor substitutor = new StringSubstitutor(mapping.toJavaMap());
        final String replaced = substitutor.replace(configuration.aaiServiceInstancePath());

        return httpGetClient.get(
                getUri(replaced),
                createRequestDiagnosticContext(),
                configuration.aaiHeaders());
    }

    private String getUri(final String endpoint) {
        return new URI.URIBuilder()
                .scheme(configuration.aaiProtocol())
                .host(configuration.aaiHost())
                .port(configuration.aaiPort())
                .path(configuration.aaiBasePath() + "/" + endpoint)
                .build()
                .toString();
    }
}