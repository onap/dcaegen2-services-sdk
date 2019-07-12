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

import io.vavr.Function2;
import java.util.function.Function;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

@Value.Immutable
@Value.Style(stagedBuilder = true)
public interface AaiTestModel<Required extends AaiModel, Complete extends AaiDeletable & AaiRelationable> {
    Class<Required> requiredType();

    Class<Complete> completeType();

    String resourceUrl();

    Required requiredModel();

    Complete completeModel();

    Function<RelationshipList, Complete> completeModelWithRelation();

    RelationshipList requiredAsRelationship();

    Function<AaiActionFactory, AaiGetAction<Required, ? extends Required>> getAction();

    Function<AaiActionFactory, AaiCreateAction<Complete>> createAction();

    Function<AaiActionFactory, AaiUpdateAction<Complete>> updateAction();

    Function<AaiActionFactory, AaiDeleteAction<Complete>> deleteAction();

    Function<AaiActionFactory, AaiGetRelationAction<AaiRelationable, Complete>> getRelationToAction();

    Function2<AaiActionFactory, Class<? extends AaiModel>, AaiAddRelationAction<Required, ? extends AaiModel>>
        addRelationFromAction();

    Function2<AaiActionFactory, Class<? extends AaiModel>, AaiDeleteRelationAction<Complete, ? extends AaiModel>>
        deleteRelationFromAction();
}
