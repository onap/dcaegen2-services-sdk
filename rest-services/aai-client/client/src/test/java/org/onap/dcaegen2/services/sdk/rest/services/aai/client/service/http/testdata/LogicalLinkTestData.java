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

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableLogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ImmutableLogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.ImmutableAaiTestModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public final class LogicalLinkTestData {
    public static final String AAI_TYPE = "logical-link";
    public static final String LINK_TYPE = "SomeLogicalLinkType";
    public static final String LINK_NAME = "SomeLogicalLinkName";
    public static final String LL_LINK = AAI_TYPE + "/" + LINK_NAME;
    public static final ImmutableLogicalLinkRequired LOGICAL_LINK_REQUIRED =
            ImmutableLogicalLinkRequired
                    .builder()
                    .linkType(LINK_TYPE)
                    .linkName(LINK_NAME)
                    .build();
    public static final ImmutableLogicalLinkComplete LOGICAL_LINK_COMPLETE =
            ImmutableLogicalLinkComplete
                    .builder()
                    .linkType(LINK_TYPE)
                    .linkName(LINK_NAME)
                    .resourceVersion("12214")
                    .build();
    public static final RelationshipList LOGICAL_LINK_RELATIONSHIP =
            ImmutableRelationshipList
                    .builder()
                    .addRelationship(
                            ImmutableRelationship
                                    .builder()
                                    .relatedLink(LL_LINK)
                                    .relatedTo("logical-link")
                                    .relationshipLabel("org.onap.relationships.inventory.BridgedTo")
                                    .addRelationshipData(
                                            ImmutableRelationshipData
                                                    .builder()
                                                    .relationshipKey(AAI_TYPE + ".link-name")
                                                    .relationshipValue(LINK_NAME)
                                                    .build()
                                    ).build()
                    ).build();
    public static final AaiTestModel<LogicalLinkRequired, LogicalLinkComplete> LOGICAL_LINK_DATA =
            ImmutableAaiTestModel
                    .<LogicalLinkRequired, LogicalLinkComplete>builder()
                    .requiredType(LogicalLinkRequired.class)
                    .completeType(LogicalLinkComplete.class)
                    .resourceUrl(LL_LINK)
                    .requiredModel(LOGICAL_LINK_REQUIRED)
                    .completeModel(LOGICAL_LINK_COMPLETE)
                    .completeModelWithRelation(LOGICAL_LINK_COMPLETE::withRelationshipList)
                    .requiredAsRelationship(LOGICAL_LINK_RELATIONSHIP)
                    .action(AaiActionFactory::getLogicalLink)
                    .createAction(AaiActionFactory::createLogicalLink)
                    .updateAction(AaiActionFactory::updateLogicalLink)
                    .deleteAction(AaiActionFactory::deleteLogicalLink)
                    .relationToAction(AaiActionFactory::getRelationToLogicalLink)
                    .addRelationFromAction(AaiActionFactory::addRelationFromLogicalLink)
                    .deleteRelationFromAction(AaiActionFactory::deleteRelationFromLogicalLink)
                    .build();

    private LogicalLinkTestData() {
    }
}
