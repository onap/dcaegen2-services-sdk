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

import io.vavr.collection.List;
import io.vavr.control.Option;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
public class ListenableCbsConfig {

    private MerkleTree<String> tree = MerkleTree.emptyWithDefaultDigest(String::getBytes);
    private final TreePathListener<String> pathListener = new TreePathListener<>();
    private final Object treeUpdateMonitor = new Object();

    public Flux<Option<MerkleTree<String>>> subtreeChanges(List<String> path) {
        return Flux.create(sink -> {
            final TreeChangeListener<String> listener = sink::next;
            sink.onDispose(() -> cancel(path, listener));
            listen(path, listener);
        });
    }

    public void listen(List<String> path, TreeChangeListener<String> listener) {
        pathListener.listen(path, listener);
    }

    public void cancel(List<String> path, TreeChangeListener<String> listener) {
        pathListener.cancel(path, listener);
    }

    public Disposable subscribeForUpdates(Flux<MerkleTree<String>> updates) {
        return updates.subscribe(this::update);
    }

    public void update(MerkleTree<String> newTree) {
        final MerkleTree<String> oldTree;

        synchronized (treeUpdateMonitor) {
            oldTree = tree;
            tree = newTree;
        }

        pathListener.update(oldTree, newTree);
    }
}
