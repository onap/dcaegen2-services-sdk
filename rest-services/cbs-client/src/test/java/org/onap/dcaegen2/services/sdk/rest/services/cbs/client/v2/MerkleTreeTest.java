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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.v2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class MerkleTreeTest {

    @Test
    void shouldBeAbleToGetEntries() {
        MerkleTree<String> cut = emptyTree()
                .add("value1", "ala", "ma", "kota")
                .add("value2", "ala", "ma", "psa");

        assertThat(cut.get("ala", "ma", "kota")).contains("value1");
        assertThat(cut.get("ala", "ma", "psa")).contains("value2");
    }

    @Test
    void shouldReturnNoneForNonExistingPaths() {
        MerkleTree<String> cut = emptyTree()
                .add("value1", "ala", "ma", "kota")
                .add("value2", "ala", "ma", "psa");

        assertThat(cut.get("ala", "je", "obiad")).isEmpty();
    }

    @Test
    void shouldReturnNoneWhenNodeDoesntContainValue() {
        MerkleTree<String> cut = emptyTree()
                .add("value1", "ala", "ma", "kota")
                .add("value2", "ala", "ma", "psa");

        assertThat(cut.get("ala", "ma")).isEmpty();
    }


    @Test
    void shouldNotCreateNewObjectWhenNothingChanged() {
        MerkleTree<String> cut = emptyTree()
                .add("some value", "ala", "ma", "kota");

        final MerkleTree<String> result = cut.add("some value", "ala", "ma", "kota");

        assertThat(result).isSameAs(cut);
    }

    @Test
    void shouldRecalculateHashesAfterAddingNewNode() {
        MerkleTree<String> cut = emptyTree()
                .add("value1", "ala", "ma", "kota")
                .add("value2", "ala", "ma", "psa")
                .add("value3", "ala", "name");

        final MerkleTree<String> modified = cut.add("value4", "ala", "surname");

        assertThat(modified).isNotSameAs(cut);

        assertThat(modified.hashOf("ala", "ma")).isEqualTo(cut.hashOf("ala", "ma"));
        assertThat(modified.hashOf("ala", "ma", "kota")).isEqualTo(cut.hashOf("ala", "ma", "kota"));
        assertThat(modified.hashOf("ala", "ma", "psa")).isEqualTo(cut.hashOf("ala", "ma", "psa"));
        assertThat(modified.hashOf("ala", "name")).isEqualTo(cut.hashOf("ala", "name"));

        assertThat(modified.hashOf("ala", "surname")).isNotEqualTo(cut.hashOf("ala", "surname"));
        assertThat(modified.hashOf("ala")).isNotEqualTo(cut.hashOf("ala"));
        assertThat(modified.hash()).isNotEqualTo(cut.hash());
    }

    private MerkleTree<String> emptyTree() {
        return MerkleTree.emptyWithDefaultDigest(String::getBytes);
    }
}