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
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MerkleTreeParserTest {

    public static final String ARTIFICIAL_NODE_PREFIX = "NODE_baKt4ZjGpp_";
    private final MerkleTreeParser cut = new MerkleTreeParser();

    @Test
    void fromJsonObject_givenEmptyJsonObject_shouldReturnEmptyMerkleTree() {
        JsonObject jsonObject = new JsonObject();

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        assertThat(tree.isSame(emptyTree())).isTrue();
    }

    @Test
    void fromJsonObject_givenSingleKeyValuePair_shouldReturnSingleNodeTree() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("p1", "v1");

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add("v1", "p1");
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
                .add("v1", "p1", "p2", "p3");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenMultipleKeyValuePairs_shouldCreateMultipleLeafs() {
        JsonObject keyValuePairs = new JsonObject();
        keyValuePairs.addProperty("A", "v");
        keyValuePairs.addProperty("B", "v");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", keyValuePairs);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add("v", "p1", "A")
                .add("v", "p1", "B");
        assertThat(tree).isEqualTo(expected);
    }

    @Test
    void fromJsonObject_givenJsonArray_shouldCreateMultipleLeafsUnderArtificialNode() {
        JsonObject singleKeyValuePair = new JsonObject();
        singleKeyValuePair.addProperty("p2", "v2");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("v1");
        jsonArray.add(singleKeyValuePair);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("p1", jsonArray);

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add("v1", "p1", ARTIFICIAL_NODE_PREFIX + "0")
                .add("v2", "p1", ARTIFICIAL_NODE_PREFIX + "1", "p2");
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
        jsonObject.add("p2", jsonArray );
        jsonObject.add("p3", jsonObject2 );

        MerkleTree<String> tree = cut.fromJsonObject(jsonObject);

        MerkleTree<String> expected = emptyTree()
                .add("v1", "p1")
                .add("v2", "p2", ARTIFICIAL_NODE_PREFIX + 0)
                .add("v3", "p2", ARTIFICIAL_NODE_PREFIX + 1)
                .add("v4", "p3", "p4");
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
                .add("1", "p1", ARTIFICIAL_NODE_PREFIX + "0")
                .add("2", "p1", ARTIFICIAL_NODE_PREFIX + "1")
                .add("3.0", "p1", ARTIFICIAL_NODE_PREFIX + "2")
                .add("true", "p1", ARTIFICIAL_NODE_PREFIX + "3")
                .add("999799799799799", "p1", ARTIFICIAL_NODE_PREFIX + "4");
        assertThat(tree).isEqualTo(expected);
    }


    @NotNull
    private MerkleTree<String> emptyTree() {
        return MerkleTree.emptyWithDefaultDigest(String::getBytes);
    }
}