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

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.MerkleTree;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class MerkleTreeTest {

    @Test
    void shouldBeAbleToGetEntries() {
        MerkleTree<String> cut = emptyTree()
                .add(List.of("ala","ma", "kota"), "value1")
                .add(List.of("ala", "ma", "psa"),"value2");

        assertThat(cut.get(List.of("ala", "ma", "kota"))).contains("value1");
        assertThat(cut.get(List.of("ala", "ma", "psa"))).contains("value2");
    }

    @Test
    void shouldReturnNoneForNonExistingPaths() {
        MerkleTree<String> cut = emptyTree()
                .add(List.of("ala", "ma", "kota"), "value1")
                .add(List.of("ala", "ma", "psa"),"value2");

        assertThat(cut.get(List.of("ala", "je", "obiad"))).isEmpty();
    }

    @Test
    void shouldReturnNoneWhenNodeDoesntContainValue() {
        MerkleTree<String> cut = emptyTree()
                .add(List.of("ala", "ma", "kota"),"value1")
                .add(List.of("ala", "ma", "psa"), "value2");

        assertThat(cut.get(List.of("ala", "ma"))).isEmpty();
    }


    @Test
    void shouldNotCreateNewObjectWhenNothingChanged() {
        MerkleTree<String> cut = emptyTree()
                .add(List.of("ala", "ma", "kota"), "some value");

        final MerkleTree<String> result = cut.add(List.of("ala", "ma", "kota"),"some value");

        assertThat(result).isSameAs(cut);
    }

    @Test
    void shouldRecalculateHashesAfterAddingNewNode() {
        MerkleTree<String> cut = emptyTree()
                .add(List.of("ala", "ma", "kota"), "value1")
                .add(List.of("ala", "ma", "psa"), "value2")
                .add(List.of("ala", "name"), "value3");

        final MerkleTree<String> modified = cut.add(List.of("ala", "surname"), "value4");

        assertThat(modified).isNotSameAs(cut);

        assertThat(modified.hashOf(List.of("ala", "ma"))).isEqualTo(cut.hashOf(List.of("ala", "ma")));
        assertThat(modified.hashOf(List.of("ala", "ma", "kota"))).isEqualTo(cut.hashOf(List.of("ala", "ma", "kota")));
        assertThat(modified.hashOf(List.of("ala", "ma", "psa"))).isEqualTo(cut.hashOf(List.of("ala", "ma", "psa")));
        assertThat(modified.hashOf(List.of("ala", "name"))).isEqualTo(cut.hashOf(List.of("ala", "name")));

        assertThat(modified.hashOf(List.of("ala", "surname"))).isNotEqualTo(cut.hashOf(List.of("ala", "surname")));
        assertThat(modified.hashOf(List.of("ala"))).isNotEqualTo(cut.hashOf(List.of("ala")));
        assertThat(modified.hash()).isNotEqualTo(cut.hash());
    }

    private MerkleTree<String> emptyTree() {
        return MerkleTree.emptyWithDefaultDigest(String::getBytes);
    }
}