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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils;

import static io.vavr.collection.Stream.of;
import static io.vavr.collection.Stream.ofAll;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType.UNKNOWN;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public final class RelationParser {
    private RelationParser() {
    }

    /**
     * Checks if RelationshipList contains a relation to specific type
     *
     * @param data RelationshipList in which relation is sought.
     * @param relation type of relation to seek (for instance `service-instance`).
     * @return optional Relationship if relation for type `relation` was found in `data`.
     * @throws NullPointerException if one of params is null or data.getRelationship() is null.
     */
    public static Stream<Relationship> findRelatedTo(RelationshipList data, String relation, RelationType relType) {

        return ofAll(data.getRelationship())
                .filter(rel -> relation.equals(rel.getRelatedTo()))
                .filter(rel -> relType.equals(UNKNOWN) || relType.type.equals(rel.getRelationshipLabel()));
    }

    /**
     * Checks if RelationshipData contains fields names as AaiType.FieldName.
     * This function removes `AaiType.` prefix from field name and creates JsonObject from them and
     * their corresponding values converted to JsonElement.
     *
     * @param converter is used for converting a relationship value to JsonElement.
     * @param data is queried for relationship values.
     * @return JsonObject containing fields that were obtained from `data`.
     * @throws NullPointerException if one of params is null or data.getRelationshipData() is null.
     */
    public static JsonObject toJsonObject(Gson converter, Relationship data) {

        return ofAll(data.getRelationshipData()).foldLeft(new JsonObject(), (acc, item) -> {

            final String key = item.getRelationshipKey();
            final String value = item.getRelationshipValue();
            final String parsedKey = of(key.split("\\.")).last();

            acc.add(parsedKey, converter.toJsonTree(value));

            return acc;
        });
    }

    /**
     * Relation type obtained from AAI API is a plain String. This function converts it to an enum.
     *
     * @param relation is queried for a relation type.
     * @return RelationType enum with value corresponding to `relation` type
     *     or RelationType.UNKNOWN if the mapping doesn't exist.
     * @throws NullPointerException if `relation` is null.
     */
    public static RelationType toRelationType(Relationship relation) {
        return of(RelationType.values())
                .find(name -> name.type.equals(relation.getRelationshipLabel()))
                .getOrElse(UNKNOWN);
    }
}