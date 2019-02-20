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

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
public class ListenableCbsConfig {

    private MerkleTree<String> tree = MerkleTree.emptyWithDefaultDigest(String::getBytes);
    private final Map<List<String>, CompositeTreeChangeListener<String>> pathListeners = new HashMap<>();
    private final Object listenersUpdateMonitor = new Object();
    private final Object treeUpdateMonitor = new Object();

    public void listen(List<String> path, TreeChangeListener<String> listener) {
        synchronized (listenersUpdateMonitor) {
            CompositeTreeChangeListener<String> compositeListener = pathListeners
                    .computeIfAbsent(path, p -> new CompositeTreeChangeListener<>());
            compositeListener.addListener(listener);
        }
    }

    public void cancel(List<String> path, TreeChangeListener<String> listener) {
        synchronized (listenersUpdateMonitor) {
            CompositeTreeChangeListener<String> compositeListener = pathListeners.get(path);
            if (compositeListener != null) {
                compositeListener.removeListener(listener);
            }
        }
    }

    public Flux<Option<MerkleTree<String>>> subtreeChanges(List<String> path) {
        return Flux.create(sink -> {
            final TreeChangeListener<String> listener = sink::next;
            sink.onDispose(() -> cancel(path, listener));
            listen(path, listener);
        });
    }

    public Mono<Void> subscribeForUpdates(Flux<MerkleTree<String>> updates) {
        return updates.doOnNext(this::update).then();
    }

    public void update(MerkleTree<String> newTree) {
        final MerkleTree<String> oldTree;
        synchronized (treeUpdateMonitor) {
            oldTree = tree;
            tree = newTree;
        }

        for (Map.Entry<List<String>, CompositeTreeChangeListener<String>> entry : pathListeners.entrySet()) {
            final List<String> path = entry.getKey();
            final CompositeTreeChangeListener<String> listeners = entry.getValue();
            if (!newTree.isSame(path, oldTree)) {
                listeners.accept(newTree, path);
            }
        }
    }

    private static class CompositeTreeChangeListener<V> implements TreeChangeListener<V> {

        private final Collection<TreeChangeListener<V>> listeners = new HashSet<>();

        void addListener(TreeChangeListener<V> listener) {
            listeners.add(listener);
        }

        void removeListener(TreeChangeListener<V> listener) {
            listeners.remove(listener);
        }

        @Override
        public void accept(Option<MerkleTree<V>> updatedSubtree) {
            for (TreeChangeListener<V> listener : listeners) {
                listener.accept(updatedSubtree);
            }
        }
    }
}
