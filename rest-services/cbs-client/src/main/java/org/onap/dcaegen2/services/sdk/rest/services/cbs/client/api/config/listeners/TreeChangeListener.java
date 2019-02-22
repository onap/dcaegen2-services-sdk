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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.config.listeners;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.function.Consumer;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.config.MerkleTree;

/**
 * The listener for changes of the {@link MerkleTree} subtree.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
@FunctionalInterface
public interface TreeChangeListener<V> extends Consumer<Option<MerkleTree<V>>> {

    /**
     * Will be called when a change in a subtree has been detected. Default implementation will extract the changed subtree
     * from the root and call {@link #accept(Option)}.
     *
     * @param updatedTree new, updated root tree
     * @param path a changed path
     */
    default void accept(MerkleTree<V> updatedTree, List<String> path) {
        accept(updatedTree.subtree(path));
    }

    /**
     * Will be called when a change in a subtree has been detected.
     *
     * @param updatedSubtree new, updated subtree or None when branch has been removed
     */
    @Override
    void accept(Option<MerkleTree<V>> updatedSubtree);
}
