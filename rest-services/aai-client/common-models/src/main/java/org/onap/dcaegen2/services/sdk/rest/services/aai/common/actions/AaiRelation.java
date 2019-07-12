/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;

@Value.Immutable
@Value.Style(stagedBuilder = true)
public interface AaiRelation<T extends AaiModel, U extends AaiModel> extends AaiModel {

    T from();

    U to();

    RelationType relationType();

    /**
     * @param from AaiModel which represents start of the relation.
     * @param to AaiModel which represents end of the relation.
     * @param relation type of relation between from and to (like ComposedOf, BridgedTo etc.).
     * @return immutable instance of AaiRelation
     */

    static <S extends AaiModel, R extends AaiModel> AaiRelation<S, R> create(S from, R to, RelationType relation) {
        return ImmutableAaiRelation
                .<S, R>builder()
                .from(from)
                .to(to)
                .relationType(relation)
                .build();
    }

    static <S extends AaiModel, R extends AaiModel> AaiRelation<S, R> create(S from, R to) {
        return create(from, to, RelationType.UNKNOWN);
    }
}
