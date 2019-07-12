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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutablePnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiHttpActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;

public class DirectTest {
    @Test
    void cos() {
       final AaiActionFactory factory =  new AaiHttpActionFactory(
               new ImmutableAaiClientConfiguration.Builder()
                       .aaiUserName("AAI")
                       .aaiUserPassword("AAI")
                       .aaiIgnoreSslCertificateErrors(true)
                       .putAaiHeaders("Authorization", "Basic QUFJOkFBSQ==")
                       .putAaiHeaders("x-fromappid", "cos")
                       .putAaiHeaders("x-transactionid", "9998")
                       .putAaiHeaders("Accept", "application/json")
                       .enableAaiCertAuth(false)
                       .trustStorePath("")
                       .trustStorePasswordPath("")
                       .keyStorePath("")
                       .keyStorePasswordPath("")
                       .baseUrl("10.183.37.205:30233/aai/v14/")
                       .build());

//        factory
//               .createPnf()
//               .call(ImmutablePnfComplete
//                       .builder()
//                       .pnfName("Stefan3")
//                       .build()
//               ).block();

        final PnfComplete pnf2 = factory
                .getPnf()
                .call(ImmutablePnfComplete
                        .builder()
                        .pnfName("Stefan3")
                        .build()
                ).block();

//       final PnfComplete pnf = com.block();
//
        AaiRelation<PnfComplete, ServiceInstanceRequired> bb = factory.getRelationToServiceInstance().callT(pnf2).block();

        System.out.println(factory.deleteRelationFromPnf(ServiceInstanceRequired.class).call(bb).block());
//
//        ImmutableAaiRelation<PnfRequired, ImmutableServiceInstanceRequired> a = ImmutableAaiRelation
//                .<PnfRequired, ImmutableServiceInstanceRequired>builder()
//                .from(pnf2)
//                .to(ImmutableServiceInstanceRequired
//                        .builder()
//                        .globalCustomerId("JanuszCustomer")
//                        .serviceInstanceId("66c20097-1b80-41c1-8312-1c22971f5f03")
//                        .serviceType("vFWCL")
//                        .build()
//                ).relationType(RelationType.UNKNOWN)
//                .build();
//
//
//        factory.addRelationPnf(ServiceInstanceRequired.class)
//               .call(a)
//               .block();


//       factory
//               .deletePnf()
//               .call(ImmutablePnfComplete
//                       .builder()
//                       .pnfName("Janusz")
//                       .resourceVersion("1562850533137")
//                       .build())
//               .block();

//       final PnfComplete pnf3 =
//               factory
//                       .getPnf()
//                       .call(ImmutablePnfRequired.builder().pnfName("Stefan2").build())
//                       .block();

       //System.out.println(factory.getRelationServiceInstance().call(pnf3).block());
       System.out.println("-----------------");
       //System.out.println(pnf);
    }
}
