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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.testdata;

import static java.lang.String.format;

import java.util.Calendar;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.ImmutableAaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public final class ServiceInstanceTestData {
    public static String AAI_TYPE = "service-instance";
    public static String CUSTOMER_ID = "SomeCustomer";
    public static String SERVICE_ID = "SomeServiceInstanceId";
    public static String SERVICE_TYPE = "SomeServiceType";
    public static String SERVICE_LINK = format(
            "/business/customers/customer/%s"
                    + "/service-subscriptions/service-subscription/%s"
                    + "/service-instances/service-instance/%s",
            CUSTOMER_ID, SERVICE_TYPE, SERVICE_ID);
    public static final RelationshipList SERVICE_INSTANCE_RELATIONSHIP =
            ImmutableRelationshipList
                    .builder()
                    .addRelationship(
                            ImmutableRelationship
                                    .builder()
                                    .relatedLink(SERVICE_LINK)
                                    .relatedTo(AAI_TYPE)
                                    .relationshipLabel("org.onap.relationships.inventory.BridgedTo")
                                    .addRelationshipData(
                                            ImmutableRelationshipData
                                                    .builder()
                                                    .relationshipKey(AAI_TYPE + ".service-instance-id")
                                                    .relationshipValue(SERVICE_ID)
                                                    .build(),
                                            ImmutableRelationshipData
                                                    .builder()
                                                    .relationshipKey("customer.global-customer-id")
                                                    .relationshipValue(CUSTOMER_ID)
                                                    .build(),
                                            ImmutableRelationshipData
                                                    .builder()
                                                    .relationshipKey("service-subscription.service-type")
                                                    .relationshipValue(SERVICE_TYPE)
                                                    .build()
                                    ).build()
                    ).build();
    public static ImmutableServiceInstanceRequired SERVICE_INSTANCE_REQUIRED =
            ImmutableServiceInstanceRequired
                    .builder()
                    .globalCustomerId(CUSTOMER_ID)
                    .serviceInstanceId(SERVICE_ID)
                    .serviceType(SERVICE_TYPE)
                    .build();

    public static ImmutableServiceInstanceComplete SERVICE_INSTANCE_COMPLETE =
            ImmutableServiceInstanceComplete
                    .builder()
                    .globalCustomerId(CUSTOMER_ID)
                    .serviceInstanceId(SERVICE_ID)
                    .serviceType(SERVICE_TYPE)
                    .serviceInstanceLocationId("some-location")
                    .serviceInstanceName("some-name")
                    .serviceRole("some-role")
                    .bandwidthTotal("122413")
                    .createdAt(Calendar.getInstance().toString())
                    .description("some-description")
                    .orchestrationStatus("ACTIVE")
                    .modelInvariantId("some-invariantId")
                    .modelVersionId("1213")
                    .resourceVersion("1124235")
                    .build();
    public static final AaiTestModel<ServiceInstanceRequired, ServiceInstanceComplete> SERVICE_INSTANCE_DATA =
            ImmutableAaiTestModel
                    .<ServiceInstanceRequired, ServiceInstanceComplete>builder()
                    .requiredType(ServiceInstanceRequired.class)
                    .completeType(ServiceInstanceComplete.class)
                    .resourceUrl(SERVICE_LINK)
                    .requiredModel(SERVICE_INSTANCE_REQUIRED)
                    .completeModel(SERVICE_INSTANCE_COMPLETE)
                    .completeModelWithRelation(SERVICE_INSTANCE_COMPLETE::withRelationshipList)
                    .requiredAsRelationship(SERVICE_INSTANCE_RELATIONSHIP)
                    .action(AaiActionFactory::getServiceInstance)
                    .createAction(AaiActionFactory::createServiceInstance)
                    .updateAction(AaiActionFactory::updateServiceInstance)
                    .deleteAction(AaiActionFactory::deleteServiceInstance)
                    .relationToAction(AaiActionFactory::getRelationToServiceInstance)
                    .addRelationFromAction(AaiActionFactory::addRelationFromServiceInstance)
                    .deleteRelationFromAction(AaiActionFactory::deleteRelationFromServiceInstance)
                    .build();

    private ServiceInstanceTestData() {
    }
}
