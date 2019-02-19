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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.collection.List;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Class responsible for creating MerkleTree structure from JsonObject.
 *
 * @since 1.1.2
 */
class MerkleTreeParser {

    /**
     * <p> This field is used to handle JsonArrays instances in structure. </p>
     * <p> Prefix is concatenated with sequential integers for each JsonArray element.
     * Label created this way is added to element's path in the tree. </p>
     * <p> This results in array split into multiple nodes in MerkleTree structure. </p>
     */
    static final String ARTIFICIAL_NODE_PREFIX = "NODE_baKt4ZjGpp_";

    /**
     * <p> Method used to parse {@link JsonObject} into {@link MerkleTree} structure.</p>
     * <p> The algorithm will recursively create mapping of path in tree to value.
     * Each JsonObject will append it's key to path until value of primitive type is encountered.</p>
     * <p> For each JsonArray element artificial path is created separately as described in {@link MerkleTreeParser#ARTIFICIAL_NODE_PREFIX}.</p>
     * <p> Mapping created by this algorithm is later used to create MerkleTree by means of {@link MerkleTree#add(List, Object)} method.</p>
     *
     * <p>Example. For JsonObject:
     * <pre>
     * {
     *      "p1": "v1",
     *      "p2": ["v2", "v3"]
     *      "p3": {
     *          "p4": "v4"
     *      }
     * }
     * </pre>
     * following map would be created:</p>
     * <pre>
     *  "v1" <- ["p1"]
     *  "v2" <- ["p2", "NODE_baKt4ZjGpp_0"]
     *  "v3" <- ["p2", "NODE_baKt4ZjGpp_1"]
     *  "v4" <- ["p3", "p4"]
     * </pre>
     *
     * @param json JsonObject to be parsed
     * @since 1.1.2
     */
    MerkleTree<String> fromJsonObject(@NotNull final JsonObject json) {
        MerkleTree<String> tree = MerkleTree.emptyWithDefaultDigest(String::getBytes);
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            tree = treeEnhancedWithEntry(tree, entry);
        }

        return tree;
    }

    private MerkleTree<String> treeEnhancedWithEntry(final MerkleTree<String> tree,
                                                     final Entry<String, JsonElement> entry) {
        final Map<List<String>, String> pathsToValues = pathsToValuesFromJsonElement(entry.getKey(), entry.getValue(), List.empty());

        return createTreeFromValuesPaths(tree, pathsToValues);
    }

    private Map<List<String>, String> pathsToValuesFromJsonElement(final String jsonKey,
                                                                   final JsonElement element,
                                                                   final List<String> elementPathPrefix) {
        final HashMap<List<String>, String> pathToValue = new HashMap<>();
        final List<String> newPrefix = elementPathPrefix.append(jsonKey);

        if (element.isJsonObject()) {
            element.getAsJsonObject()
                    .entrySet()
                    .forEach(entry ->
                            pathToValue.putAll(pathsToValuesFromJsonElement(entry.getKey(), entry.getValue(), newPrefix))
                    );
        } else if (element.isJsonArray()) {
            pathToValue.putAll(handleArray(newPrefix, element.getAsJsonArray()));
        } else if (element.isJsonPrimitive()) {
            pathToValue.put(newPrefix, element.getAsString());
        } else if (element.isJsonNull()) {
            pathToValue.put(newPrefix, null);
        }
        return pathToValue;
    }

    private HashMap<List<String>, String> handleArray(List<String> newPrefix, JsonArray jsonArray) {
        final HashMap<List<String>, String> hashMap = new HashMap<>();
        int labelIndex = 0;

        for (JsonElement jsonElement : jsonArray) {
            hashMap.putAll(pathsToValuesFromJsonElement(ARTIFICIAL_NODE_PREFIX + labelIndex++, jsonElement, newPrefix));
        }
        return hashMap;
    }

    private MerkleTree<String> createTreeFromValuesPaths(MerkleTree<String> tree,
                                                         final Map<List<String>, String> pathToValue) {
        for (Entry<List<String>, String> entry : pathToValue.entrySet()) {
            List<String> path = entry.getKey();
            String value = entry.getValue();
            tree = tree.add(path, value);
        }
        return tree;
    }
}
