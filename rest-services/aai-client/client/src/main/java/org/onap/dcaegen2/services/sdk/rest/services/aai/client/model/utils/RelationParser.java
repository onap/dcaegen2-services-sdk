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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils;

import static io.vavr.collection.Stream.of;
import static io.vavr.collection.Stream.ofAll;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public final class RelationParser {
    private RelationParser() {
    }

    public static Option<Relationship> findRelatedTo(RelationshipList data, final String relation) {
        return ofAll(data.getRelationship()).find(x -> relation.equals(x.getRelatedTo()));
    }

    public static JsonObject toJsonObject(Relationship data, Gson gson) {

        return ofAll(data.getRelationshipData()).foldLeft(new JsonObject(), (acc, item) -> {

            final String key = item.getRelationshipKey();
            final String value = item.getRelationshipValue();
            final String parsedKey = of(key.split("\\.")).last();

            acc.add(parsedKey, gson.toJsonTree(value));

            return acc;
        });
    }

    public static RelationType toRelationType(String relationType) {
        return of(RelationType.values())
                .find(name -> name.type.equals(relationType))
                .getOrElse(RelationType.UNKNOWN);
    }
}
