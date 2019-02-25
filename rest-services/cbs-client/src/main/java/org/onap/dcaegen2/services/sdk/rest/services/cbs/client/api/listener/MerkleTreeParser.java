/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.jetbrains.annotations.NotNull;


/**
 * Class responsible for creating MerkleTree structure from JsonObject.
 *
 * @since 1.1.2
 */
class MerkleTreeParser {

    MerkleTree<String> fromJsonObject(final @NotNull JsonObject json) {
        Map<List<String>, String> pathToValue =
                new JsonObjectWrapper(json)
                        .convertJsonObjectsAndArraysToPaths()
                        .getPrimitivesAsMap()
                        .getOrElse(HashMap::empty)
                        .mapValues(JsonElement::getAsJsonPrimitive)
                        .mapValues(JsonPrimitive::getAsString);
        return createTreeFromValuesPaths(MerkleTree.emptyWithDefaultDigest(String::getBytes), pathToValue);
    }

    private MerkleTree<String> createTreeFromValuesPaths(MerkleTree<String> tree,
                                                         final Map<List<String>, String> pathToValue) {
        for (Tuple2<List<String>, String> entry : pathToValue) {
            List<String> path = entry._1;
            String value = entry._2;
            tree = tree.add(path, value);
        }
        return tree;
    }
}
