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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.vavr.collection.List;
import java.math.BigInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class MerkleTreeParserTest {

    private final MerkleTreeParser cut = new MerkleTreeParser();

    @Test
    void fromJsonObject_givenEmptyJsonObject_shouldReturnEmptyMerkleTree() {
        JsonObject jsonObject = new JsonObject();

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);
        assertThat(tree.isSame(List.empty(), emptyTree())).isTrue();
    }

    @Test
    void fromJsonObject_givenSingleKeyValuePair_shouldReturnSingleNodeTree() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("p1", "v1");

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1"), "v1");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenSingleKeyValuePair_atDeeperPathLevel_shouldReturnTreeWithSingleLeafAndCorrectNodesOnTheWay() {
        JsonObject singleKeyValuePair = new JsonObject();
        singleKeyValuePair.addProperty("p3", "v1");
        JsonObject intermediateNode = new JsonObject();
        intermediateNode.add("p2", singleKeyValuePair);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", intermediateNode);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1", "p2", "p3"), "v1");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenMultipleKeyValuePairs_shouldCreateMultipleLeafs() {
        JsonObject keyValuePairs = new JsonObject();
        keyValuePairs.addProperty("A", "vA");
        keyValuePairs.addProperty("B", "vB");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", keyValuePairs);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1", "A"), "vA")
                .add(List.of("p1", "B"), "vB");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenJsonArray_shouldCreateMultipleLeafsUnderArtificialNodes() {
        JsonObject singleKeyValuePair = new JsonObject();
        singleKeyValuePair.addProperty("p2", "v2");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("v1");
        jsonArray.add(singleKeyValuePair);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", jsonArray);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1", "0"), "v1")
                .add(List.of("p1", "1", "p2"), "v2");
        assertThat(tree).isEqualTo(expected);
    }


    @Test
    void fromJsonObject_givenMoreComplicatedJson_shouldReturnCorrectTree() {
        // below example is contained in javadoc for method
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("p4", "v4");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("v2");
        jsonArray.add("v3");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("p1", "v1");
        jsonObject.add("p2", jsonArray);
        jsonObject.add("p3", jsonObject2);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1"), "v1")
                .add(List.of("p2", "0"), "v2")
                .add(List.of("p2", "1"), "v3")
                .add(List.of("p3", "p4"), "v4");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenNotStringValues_shouldCastAllToString() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(1);
        jsonArray.add(2L);
        jsonArray.add(3.0);
        jsonArray.add(true);
        jsonArray.add(new BigInteger("999799799799799"));
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", jsonArray);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add(List.of("p1", "0"), "1")
                .add(List.of("p1", "1"), "2")
                .add(List.of("p1", "2"), "3.0")
                .add(List.of("p1", "3"), "true")
                .add(List.of("p1", "4"), "999799799799799");
        assertThat(tree).isEqualTo(expected);
    }

    @NotNull
    private MerkleTree<String> emptyTree() {
        return MerkleTree.emptyWithDefaultDigest(String::getBytes);
    }
}