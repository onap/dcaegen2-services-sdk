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

import io.vavr.control.Option;
import java.util.Collection;
import java.util.HashSet;

/**
 * @since 1.1.2
 *
 * NOTE
 * The class is thread unsafe
 */
class CompositeTreeChangeListener<V> implements TreeChangeListener<V> {

    private final Collection<TreeChangeListener<V>> listeners = new HashSet<>();

    public void addListener(TreeChangeListener<V> listener) {
        listeners.add(listener);
    }

    public void removeListener(TreeChangeListener<V> listener) {
        listeners.remove(listener);
    }

    @Override
    public void accept(Option<MerkleTree<V>> updatedSubtree) {
        for (TreeChangeListener<V> listener : listeners) {
            listener.accept(updatedSubtree);
        }
    }
}
