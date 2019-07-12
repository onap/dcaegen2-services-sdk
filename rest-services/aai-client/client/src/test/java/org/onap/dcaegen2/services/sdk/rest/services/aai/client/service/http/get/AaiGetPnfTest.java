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
//import io.vavr.collection.HashMap;
//import io.vavr.collection.Map;
//import org.junit.jupiter.api.Test;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.common.*;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.ImmutableLogicalLink;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.logicallink.LogicalLink;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.ImmutablePnf;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.pnf.Pnf;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.AnnotationProcessor;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.RelationLinkParser;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.AaiActionFactory;
//import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpActionFactory;
////import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AbstractAaiPnfTest;
//
//import java.net.MalformedURLException;
//import java.net.URI;
//import java.util.Base64;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//
//class AaiGetPnfTest { //extends AbstractAaiPnfTest {
//    private final Map<String, String> headers = HashMap.of("sample-key", "sample-value");
//    private final Pnf result;
//    //private AaiGetPnf cut;
//
//    interface Dupa1 {
//
//    }
//
//    interface Dupa2 {
//
//    }
//
//    interface Dupa4 {
//
//    }
//
//    abstract class ADupa5 implements Dupa4 {
//
//    }
//
//    class CDupa5 extends ADupa5 implements Dupa2 {
//
//    }
//
//    AaiGetPnfTest() {
//        result = ImmutablePnf
//                .builder()
//                .pnfName("Some")
//                .build();
//    }
//
//    @Test
//    void cos() throws MalformedURLException {
//
//        Class<?> a = new CDupa5().getClass();
//        System.out.println(a);
//        AnnotationProcessor.checkForAaiAnnotation(result);
//
//        final Pnf pnf =
//                ImmutablePnf
//                        .builder()
//                        .pnfName("PAAAA")
//                        .build();
//
//        final LogicalLink link =
//                ImmutableLogicalLink
//                        .builder()
//                        .linkName("AAAAA4")
//                        .linkType("a")
//                        .build();
////                        .relationshipList(
////                                ImmutableRelationshipList
////                                        .builder()
////                                        .addRelationship(
////                                                ImmutableRelationship
////                                                        .builder()
////                                                        .relatedLink("/aai/v12/business/customers/customer/Demonstration/service-subscriptions/service-subscription/vFW/service-instances/service-instance/8adc95d2-01ed-4fee-8465-bd6f21b9108e")
////                                                        .relatedTo("service-instance")
////                                                        .relationshipLabel(RelationType.COMPOSED_OF.type)
////                                                        .addRelationshipData(
////                                                                ImmutableRelationshipData
////                                                                    .builder()
////                                                                    .relationshipKey("customer.global-customer-id")
////                                                                    .relationshipValue("Demonstration")
////                                                                    .build()
////                                                        ).addRelationshipData(
////                                                            ImmutableRelationshipData
////                                                                .builder()
////                                                                .relationshipKey("service-subscription.service-type")
////                                                                .relationshipValue("vFW")
////                                                                .build()
////                                                ).addRelationshipData(
////                                                        ImmutableRelationshipData
////                                                                .builder()
////                                                                .relationshipKey("service-instance.service-instance-id")
////                                                                .relationshipValue("id123")
////                                                                .build()
////                                        ).build()
////                        ).build()).build();
////        System.out.println(link.findRelation(ServiceInstanceRequired.class, RelationType.COMPOSED_OF));
//
//
//        AaiActionFactory factory = new AaiHttpActionFactory(
//                ImmutableAaiClientConfiguration
//                    .of("10.181.138.88:30233/aai/v14/",
//                            "AAI",
//                            "AAI", false,
//                            HashMap.of(
//                                    "x-fromappid", "myApp",
//                                    "x-transactionid", "9999",
//                                    "Authorization", "Basic " + Base64.getEncoder().encodeToString("AAI:AAI".getBytes())
//                                    ).toJavaMap(),
//                            "","","","",false
//                    ));
//
//        //reactor.core.Exceptions
//
//        //factory.createPnf().call(pnf).block();
//        //factory.addRelation().call(pnf, link, RelationType.COMPOSED_OF).block();
//
//
//        Relationship r  = ImmutableRelationship
//                                                        .builder()
//                                                        .relatedLink("/aai/v14/business/customers/customer/Demonstration/service-subscriptions/service-subscription/vFW/service-instances/service-instance/8adc95d2-01ed-4fee-8465-bd6f21b9108e")
//                                                        .relatedTo("service-instance")
//                                                        .relationshipLabel(RelationType.COMPOSED_OF.type)
//                                                        .addRelationshipData(
//                                                                ImmutableRelationshipData
//                                                                    .builder()
//                                                                    .relationshipKey("customer.global-customer-id")
//                                                                    .relationshipValue("Demonstration")
//                                                                    .build()
//                                                        ).addRelationshipData(
//                                                            ImmutableRelationshipData
//                                                                .builder()
//                                                                .relationshipKey("service-subscription.service-type")
//                                                                .relationshipValue("vFW")
//                                                                .build()
//                                                ).addRelationshipData(
//                                                        ImmutableRelationshipData
//                                                                .builder()
//                                                                .relationshipKey("service-instance.service-instance-id")
//                                                                .relationshipValue("id123")
//                                                                .build()
//                                        ).build();
//
//        System.out.println(URI.create("/aai/v14/network/logical-links/logical-link/AAAAA4"));
//        URI b = URI.create("//10.144.1.10:8080/aai/v14/");
//
//        System.out.println(RelationLinkParser.parseLink(b, r));
//
////        final Gson adapter = gsonWithImmutablesAdapters();
////        final String out = adapter.toJson(link);
////
////        System.out.println(adapter.fromJson(out, LogicalLink.class));
//    }
////
////    @BeforeEach
////    protected void setUp() {
////        super.setUp();
////        cut = new AaiGetPnf(
////                headers,
////                pnfEndpoint,
////                httpClient);
////    }
////
////    @Test
////    void getResponseFromAai_shouldCallGetMethod_withGivenHeaders() {
////
////        given(response.statusCode()).willReturn(200);
////        given(response.successful()).willReturn(true);
////        given(response.bodyAsString()).willReturn(gsonWithImmutablesAdapters().toJson(result));
////        given(response.bodyAsJson(any(), any(), eq(Pnf.class))).willReturn(result);
////
////        StepVerifier
////                .create(cut.call(aaiModel))
////                .assertNext(x -> assertEquals(x.getPnfName(), pnfName))
////                .verifyComplete();
////
////        assertIfPathCorrect();
////    }
////
////    @Test
////    void getAaiResponse_shouldGenerateException() {
////
////        given(response.statusCode()).willReturn(404);
////        given(response.successful()).willReturn(false);
////
////        StepVerifier
////                .create(cut.call(aaiModel))
////                .expectError(AaiException.class);
////
////        assertIfPathCorrect();
////    }
////
////    private void assertIfPathCorrect() {
////        verify(httpClient)
////                .call(argThat(x ->
////                        x.url().contains(pnfEndpoint)
////                                && x.method() == HttpMethod.GET
////                                && x.customHeaders().containsAll(headers)));
////    }
//}
