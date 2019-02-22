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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.collection.List;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.String.valueOf;


/**
 * Class responsible for creating MerkleTree structure from JsonObject.
 *
 * @since 1.1.2
 */
class MerkleTreeParser {

    /**
     * <p> Method used to parse {@link JsonObject} into {@link MerkleTree} structure.</p>
     * <p> The algorithm will recursively create mapping of (path in tree)->(value) from JsonObject
     * and use it to create MerkleTree by means of {@link MerkleTree#add(List, Object)} method. </p>
     * <p> Each JsonObject will append it's key to path until value of primitive type is encountered.
     * For each JsonArray element artificial path is created by creating lables from sequential integers.
     * This results in array split into multiple nodes in MerkleTree structure.</p>
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
     *  "v2" <- ["p2", "0"]
     *  "v3" <- ["p2", "1"]
     *  "v4" <- ["p3", "p4"]
     * </pre>
     *
     * @param json JsonObject to be parsed
     * @since 1.1.2
     */
    MerkleTree<String> fromJsonObject(final @NotNull JsonObject json) {
        MerkleTree<String> tree = MerkleTree.emptyWithDefaultDigest(String::getBytes);
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            tree = treeEnhancedWithEntry(tree, entry);
        }

        return tree;
    }

    private MerkleTree<String> treeEnhancedWithEntry(final MerkleTree<String> tree,
                                                     final Entry<String, JsonElement> entry) {
        return createTreeFromValuesPaths(tree, pathsToValues(entry, List.empty()));
    }

    private Map<List<String>, String> pathsToValues(Entry<String, JsonElement> entry, List<String> elementPathPrefix) {
        return pathsToValuesFromJsonElement(entry.getKey(), entry.getValue(), elementPathPrefix);
    }

    private Map<List<String>, String> pathsToValuesFromJsonElement(final String jsonKey,
                                                                   final JsonElement element,
                                                                   final List<String> elementPathPrefix) {
        final HashMap<List<String>, String> pathToValue = new HashMap<>();
        final List<String> newPrefix = elementPathPrefix.append(jsonKey);

        if (element.isJsonObject()) {
            element.getAsJsonObject()
                    .entrySet()
                    .forEach(entry -> pathToValue.putAll(pathsToValues(entry, newPrefix)));
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
            String jsonKey = valueOf(labelIndex++);
            hashMap.putAll(pathsToValuesFromJsonElement(jsonKey, jsonElement, newPrefix));
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
