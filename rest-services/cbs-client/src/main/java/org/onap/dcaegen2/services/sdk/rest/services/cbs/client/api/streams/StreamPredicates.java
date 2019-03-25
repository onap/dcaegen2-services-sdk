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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams;

import java.util.Objects;
import java.util.function.Predicate;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.StreamType;

/**
 * A small collection of predicates usable when filtering {@link RawDataStream}s.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class StreamPredicates {

    public StreamPredicates() {
    }

    /**
     * Predicate for matching {@link RawDataStream} by name.
     *
     * @param name data stream name
     * @param <T> type of data stream
     * @return a predicate which returns true only when a stream name is equal to the given name
     */
    public static <T> Predicate<RawDataStream<T>> streamWithName(String name) {
        return stream -> Objects.equals(stream.name(), name);
    }

    /**
     * Predicate for matching {@link RawDataStream} by type.
     *
     * @param type data stream type
     * @param <T> type of data stream
     * @return a predicate which returns true only when a stream type is equal to the given type
     */
    public static <T> Predicate<RawDataStream<T>> streamOfType(StreamType type) {
        return stream -> stream.type() == type;
    }
}
