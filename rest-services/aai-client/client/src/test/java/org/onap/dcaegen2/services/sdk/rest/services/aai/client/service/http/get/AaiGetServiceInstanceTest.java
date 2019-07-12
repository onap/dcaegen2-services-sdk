///*
// * ============LICENSE_START=======================================================
// * DCAEGEN2-SERVICES-SDK
// * ================================================================================
// * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
// * ================================================================================
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * ============LICENSE_END=========================================================
// */
//package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import AaiException;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ImmutableServiceInstance;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.service.ServiceInstance;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AbstractHttpClientTest;
//import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
//import reactor.test.StepVerifier;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.JsonConverters.gsonWithImmutablesAdapters;
//
//class AaiGetServiceInstanceTest extends AbstractHttpClientTest {
//    private final static String endpoint = "foo/${customer}/bar/${serviceType}/baz/${serviceInstanceId}";
//    private final static String serviceId = "boz";
//
//    private final ServiceInstance aaiModel =
//            ImmutableServiceInstance
//                    .builder()
//                    .serviceInstanceId(serviceId)
//                    .serviceType("foz")
//                    .globalCustomerId("boo")
//                    .build();
//    private ServiceInstance result;
//    private AaiGetServiceInstance cut;
//
//    @BeforeEach
//    protected void setUp() {
//        super.setUp();
//        result =
//                ImmutableServiceInstance
//                        .builder()
//                        .serviceInstanceId(serviceId)
//                        .serviceType("foz")
//                        .globalCustomerId("boo")
//                        .build();
//
//        cut = new AaiGetServiceInstance(
//                headers,
//                endpoint,
//                httpClient);
//    }
//
//    @Test
//    void getAaiResponse_shouldCallGetMethod_withGivenAaiHeaders() {
//
//
//        given(response.statusCode()).willReturn(200);
//        given(response.successful()).willReturn(true);
//        given(response.bodyAsString()).willReturn(gsonWithImmutablesAdapters().toJson(result));
//        given(response.bodyAsJson(any(), any(), eq(ServiceInstance.class))).willReturn(result);
//
//        StepVerifier
//                .create(cut.call(aaiModel))
//                .assertNext(x -> assertEquals(x, result))
//                .verifyComplete();
//
//        assertIfPathCorrect();
//    }
//
//    @Test
//    void getAaiResponse_shouldGenerateException() {
//
//        given(response.statusCode()).willReturn(400);
//        given(response.successful()).willReturn(false);
//
//        StepVerifier
//                .create(cut.call(aaiModel))
//                .verifyError(AaiException.class);
//
//        assertIfPathCorrect();
//    }
//
//    private void assertIfPathCorrect() {
//        verify(httpClient)
//                .call(argThat(x ->
//                        !x.url().matches("\\$\\{.*\\}")
//                                && x.method() == HttpMethod.GET
//                                && x.customHeaders().containsAll(headers)));
//    }
//}
