/*
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration.Builder;

import com.google.gson.Gson;
import io.vavr.collection.HashMap;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.testdata.LogicalLinkTestData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.testdata.PnfTestData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.testdata.ServiceInstanceTestData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractAaiHttpFactoryTest {
    protected static final List<AaiTestModel<? extends AaiModel, ?>> TEST_MODELS = List.of(
            PnfTestData.PNF_DATA,
            LogicalLinkTestData.LOGICAL_LINK_DATA,
            ServiceInstanceTestData.SERVICE_INSTANCE_DATA);

    protected final Map<String, String> headers = HashMap.of("sample-key", "sample-value");
    protected final String baseUrl = "10.183.37.205:30233/aai/v14/";
    protected final RxHttpClient httpClient = mock(RxHttpClient.class);
    protected final HttpResponse response = mock(HttpResponse.class);
    protected final AaiClientConfiguration configuration = new Builder()
            .aaiUserName("AAI")
            .aaiUserPassword("AAI")
            .aaiIgnoreSslCertificateErrors(true)
            .putAaiHeaders("x-fromappid", "cos")
            .putAaiHeaders("x-transactionid", "9998")
            .putAaiHeaders("Accept", "application/json")
            .putAllAaiHeaders(headers.toJavaMap())
            .enableAaiCertAuth(false)
            .trustStorePath("")
            .trustStorePasswordPath("")
            .keyStorePath("")
            .keyStorePasswordPath("")
            .baseUrl(baseUrl)
            .build();

    protected final AaiHttpActionFactory cut = new AaiHttpActionFactory(
        configuration,
        () -> httpClient,
        () -> httpClient,
        omit -> httpClient);

    protected final Gson converter = cut.getConverter();


    @BeforeEach
    protected void setUp() {
        reset(httpClient, response);

        given(httpClient.call(any())).willReturn(Mono.just(response));
    }

    protected List<Arguments> provideArgsForGetAction() {
        return TEST_MODELS.map(data -> of(
                data.requiredModel(),
                data.completeModel(),
                data.resourceUrl(),
                data.getAction().apply(cut)));
    }

    protected List<Arguments> provideArgsForCreateAction() {
        return TEST_MODELS.map(data -> of(data.completeModel(), data.resourceUrl(), data.createAction().apply(cut)));
    }

    protected List<Arguments> provideArgsForUpdateAction() {
        return TEST_MODELS.map(data -> of(data.completeModel(), data.resourceUrl(), data.updateAction().apply(cut)));
    }

    protected List<Arguments> provideArgsForDeleteAction() {
        return TEST_MODELS.map(data -> of(data.completeModel(), data.resourceUrl(), data.deleteAction().apply(cut)));
    }

    protected Iterator<Arguments> provideArgsForEmptyGetRelationAction() {
        return TEST_MODELS
                .crossProduct()
                .map(pair -> of(pair._1.completeModel(), pair._2.getRelationToAction().apply(cut)));
    }

    protected Iterator<Arguments> provideArgsForGetRelationAction() {
        return TEST_MODELS
                .crossProduct()
                .map(pair -> {
                    final AaiTestModel<?, ?> from = pair._1;
                    final AaiTestModel<?, ?> to = pair._2;

                    return of(
                            from.completeModelWithRelation().apply(to.requiredAsRelationship()),
                            to.completeModel(),
                            to.resourceUrl(),
                            to.getRelationToAction().apply(cut));
                });
    }

    protected Iterator<Arguments> provideArgsForAddRelationFromAction() {
        return TEST_MODELS
                .crossProduct()
                .map(pair -> of(
                        pair._1.requiredModel(),
                        pair._2.requiredModel(),
                        pair._1.resourceUrl(),
                        pair._2.resourceUrl(),
                        pair._1.addRelationFromAction().apply(cut, pair._2.requiredType())));
    }

    protected Iterator<Arguments> provideArgsForDeleteEmptyRelationFromAction() {
        return TEST_MODELS
                .crossProduct()
                .map(pair -> of(
                        pair._1.completeModel(),
                        pair._2.requiredModel(),
                        pair._1.deleteRelationFromAction().apply(cut, pair._2.requiredType())));
    }

    protected Iterator<Arguments> provideArgsForDeleteRelationFromAction() {
        return TEST_MODELS
                .crossProduct()
                .map(pair -> of(
                        pair._1.completeModelWithRelation().apply(pair._2.requiredAsRelationship()),
                        pair._2.requiredModel(),
                        pair._1.resourceUrl(),
                        pair._2.resourceUrl(),
                        pair._1.deleteRelationFromAction().apply(cut, pair._2.requiredType())));
    }

    protected <T> void prepareResponseWithObjectAsJson(T obj) {
        final String reqBody = converter.toJson(obj);

        given(response.successful()).willReturn(true);
        given(response.statusCode()).willReturn(200);
        given(response.bodyAsString()).willReturn(reqBody);
        given(response.bodyAsString()).willReturn(reqBody);
        given(response.bodyAsJson(any()))
                .willAnswer(args -> converter.fromJson(reqBody, args.getArgument(0)));

        given(response.bodyAsJson(any(), any(), any()))
                .willAnswer(args -> ((Gson) args.getArgument(1)).fromJson(reqBody, args.getArgument(2)));
    }
}