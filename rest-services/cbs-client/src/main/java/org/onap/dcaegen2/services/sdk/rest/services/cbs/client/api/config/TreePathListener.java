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
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.1.2
 */
class TreePathListener<T> {

    private final Map<List<String>, CompositeTreeChangeListener<T>> pathListeners = new HashMap<>();

    public synchronized void listen(List<String> path, TreeChangeListener<T> listener) {
        CompositeTreeChangeListener<T> compositeListener = pathListeners
                .computeIfAbsent(path, p -> new CompositeTreeChangeListener<>());

        compositeListener.addListener(listener);
    }

    public synchronized void cancel(List<String> path, TreeChangeListener<T> listener) {
        CompositeTreeChangeListener<T> compositeListener = pathListeners.get(path);
        if (compositeListener != null) {
            compositeListener.removeListener(listener);
        }
    }

    public void update(MerkleTree<T> oldTree, MerkleTree<T> newTree) {
        pathListeners.forEach((path, listener) -> {
            if (!newTree.isSame(path, oldTree)) {
                listener.accept(newTree, path);
            }
        });
    }
}
