/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;

public abstract class AaiAbstractActionFactory {
    protected abstract <T extends AaiModel>
        AaiCreateAction<T> create(Class<T> type);

    protected abstract <T extends AaiModel>
        AaiUpdateAction<T> update(Class<T> type);

    protected abstract <T extends AaiDeletable>
        AaiDeleteAction<T> delete(Class<T> type);

    protected abstract <T extends AaiModel, U extends T>
        AaiGetAction<T, U> get(Class<T> req, Class<U> full);

    protected abstract <T extends AaiRelationable, U extends AaiModel>
        AaiGetRelationAction<T, U> getRelation(Class<U> to);

    protected abstract <T extends AaiModel, U extends AaiModel>
        AaiAddRelationAction<T, U> addRelation(Class<T> from, Class<U> to);

    protected abstract <T extends AaiDeletable & AaiRelationable, U extends AaiModel>
        AaiDeleteRelationAction<T, U> deleteRelation(Class<T> from, Class<U> to);
}
