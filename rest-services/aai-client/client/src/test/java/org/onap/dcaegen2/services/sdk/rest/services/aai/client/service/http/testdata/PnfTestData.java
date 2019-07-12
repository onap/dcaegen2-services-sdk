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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutablePnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutablePnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.ImmutableAaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public final class PnfTestData {
    public static final String AAI_TYPE = "pnf";
    public static final String PNF_NAME = "SomePnfName";
    public static final String PNF_LINK = AAI_TYPE + "/" + PNF_NAME;
    public static final ImmutablePnfRequired PNF_REQUIRED =
            ImmutablePnfRequired
                    .builder()
                    .pnfName(PNF_NAME)
                    .build();
    public static final ImmutablePnfComplete PNF_COMPLETE =
            ImmutablePnfComplete
                    .builder()
                    .pnfName(PNF_NAME)
                    .adminStatus("some")
                    .resourceVersion("134")
                    .equipModel("model")
                    .frameId("12")
                    .equipType("type1")
                    .ipaddressV4Oam("192.168.0.1")
                    .ipaddressV4Loopback0("127.0.0.1")
                    .ipaddressV6Oam("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                    .ipaddressV6Loopback0("1::1")
                    .modelVersionId("1")
                    .modelCustomizationId("uuid1")
                    .serialNumber("uuid2")
                    .build();
    public static final RelationshipList PNF_RELATIONSHIP =
            ImmutableRelationshipList
                    .builder()
                    .addRelationship(
                            ImmutableRelationship
                                    .builder()
                                    .relatedLink(PNF_LINK)
                                    .relatedTo(AAI_TYPE)
                                    .relationshipLabel("org.onap.relationships.inventory.ComposedOf")
                                    .addRelationshipData(
                                            ImmutableRelationshipData
                                                    .builder()
                                                    .relationshipKey(AAI_TYPE + ".pnf-name")
                                                    .relationshipValue(PNF_NAME)
                                                    .build()
                                    ).build()

                    ).build();
    public static final AaiTestModel<PnfRequired, PnfComplete> PNF_DATA =
            ImmutableAaiTestModel
                    .<PnfRequired, PnfComplete>builder()
                    .requiredType(PnfRequired.class)
                    .completeType(PnfComplete.class)
                    .resourceUrl(PNF_LINK)
                    .requiredModel(PNF_REQUIRED)
                    .completeModel(PNF_COMPLETE)
                    .completeModelWithRelation(PNF_COMPLETE::withRelationshipList)
                    .requiredAsRelationship(PNF_RELATIONSHIP)
                    .action(AaiActionFactory::getPnf)
                    .createAction(AaiActionFactory::createPnf)
                    .updateAction(AaiActionFactory::updatePnf)
                    .deleteAction(AaiActionFactory::deletePnf)
                    .relationToAction(AaiActionFactory::getRelationToPnf)
                    .addRelationFromAction(AaiActionFactory::addRelationFromPnf)
                    .deleteRelationFromAction(AaiActionFactory::deleteRelationFromPnf)
                    .build();

    private PnfTestData() {
    }
}
