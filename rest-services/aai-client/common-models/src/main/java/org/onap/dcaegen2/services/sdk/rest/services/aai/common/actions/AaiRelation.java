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

package org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions;

import static java.lang.String.format;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;

public final class AaiRelation<T extends AaiModel, U extends AaiModel> implements AaiModel {

    private final T from;
    private final U to;
    private final RelationType relationType;

    public AaiRelation(T from, U to, RelationType relationType) {
        this.to = to;
        this.from = from;
        this.relationType = relationType;
    }

    public AaiRelation(T from, U to) {
        this(from, to, RelationType.UNKNOWN);
    }

    public T from() {
        return from;
    }

    public U to() {
        return to;
    }

    public RelationType relationType() {
        return relationType;
    }

    @Override
    public String toString() {
        return format("%s (%s)-> %s", from(), relationType, to());
    }
}
