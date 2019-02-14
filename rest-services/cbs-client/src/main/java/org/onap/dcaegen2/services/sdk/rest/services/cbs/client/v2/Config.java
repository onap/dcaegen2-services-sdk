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

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.util.function.Consumer;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
public class Config {

    private MerkleTree<String> tree = MerkleTree.emptyWithDefaultDigest(String::getBytes);
    private Map<List<String>, Set<TreeChangeListener<String>>> pathListeners = HashMap.empty();
    private final Object listenersUpdateMonitor = new Object();
    private final Object treeUpdateMonitor = new Object();

    public void listen(List<String> path, TreeChangeListener<String> listener) {
        synchronized (listenersUpdateMonitor) {
            pathListeners = pathListeners.put(path, HashSet.of(listener), Set::addAll);
        }
    }

    public Flux<Option<MerkleTree<String>>> subtreeChanges(List<String> path) {
        return Flux.create(sink -> listen(path, sink::next));
    }

    public void update(MerkleTree<String> newTree) {
        final MerkleTree<String> oldTree;
        synchronized (treeUpdateMonitor) {
            oldTree = tree;
            tree = newTree;
        }

        for (Tuple2<List<String>, Set<TreeChangeListener<String>>> entry : pathListeners) {
            final List<String> path = entry._1();
            final Set<TreeChangeListener<String>> listeners = entry._2();
            if (!newTree.isSame(path, oldTree)) {
                notifyListeners(listeners, newTree, path);
            }
        }
    }

    private void notifyListeners(Iterable<TreeChangeListener<String>> listeners,
            MerkleTree<String> newTree,
            List<String> path) {

        for (TreeChangeListener<String> listener : listeners) {
            listener.accept(newTree, path);
        }
    }

    @FunctionalInterface
    public interface TreeChangeListener<V> extends Consumer<Option<MerkleTree<V>>> {

        default void accept(MerkleTree<V> newTree, List<String> path) {
            accept(newTree.subtree(path));
        }

        @Override
        void accept(Option<MerkleTree<V>> newSubtree);
    }
}
