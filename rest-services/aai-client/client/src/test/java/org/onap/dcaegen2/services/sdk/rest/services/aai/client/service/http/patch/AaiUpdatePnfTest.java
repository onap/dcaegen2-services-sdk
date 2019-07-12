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
//package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch;
//
//import io.vavr.collection.HashMap;
//import io.vavr.collection.Map;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AbstractAaiPnfTest;
//import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
//import reactor.test.StepVerifier;
//
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.verify;
//
//class AaiUpdatePnfTest extends AbstractAaiPnfTest {
//    private final Map<String, String> DEFAULT_PATCH_HEADERS =
//            HashMap.of("Content-Type", "application/merge-patch+json");
//    private final Map<String, String> expectedHeaders = DEFAULT_PATCH_HEADERS.merge(headers);
//    private AaiUpdatePnf cut;
//
//    @BeforeEach
//    protected void setUp() {
//        super.setUp();
//        cut = new AaiUpdatePnf(
//                headers,
//                pnfEndpoint,
//                httpClient);
//    }
//
//    @Test
//    void getResponseFromAai_shouldCallPatchMethod_withGivenHeaders_combinedWithContentType() {
//
//        given(response.statusCode()).willReturn(200);
//        given(response.successful()).willReturn(true);
//
//        StepVerifier
//                .create(cut.call(aaiModel))
//                .verifyComplete();
//
//        assertIfPathCorrect();
//    }
//
//    @Test
//    void getResponseFromAai_shouldGenerateException() {
//
//        given(response.statusCode()).willReturn(404);
//        given(response.successful()).willReturn(false);
//
//        StepVerifier
//                .create(cut.call(aaiModel))
//                .verifyError();
//
//        assertIfPathCorrect();
//    }
//
//    private void assertIfPathCorrect() {
//        verify(httpClient)
//                .call(argThat(x ->
//                        x.url().contains(pnfEndpoint)
//                     && x.method() == HttpMethod.PATCH
//                     && x.customHeaders().containsAll(expectedHeaders)));
//    }
//}
