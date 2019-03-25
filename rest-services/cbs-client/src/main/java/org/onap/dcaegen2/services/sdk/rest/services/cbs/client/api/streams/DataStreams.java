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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.collection.Stream;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;

/**
 * Extract streams from the application configuration represented as GSON JsonObject.
 *
 * Example input:
 * <pre>
 * {
 *     "application_config_1": "value_1,
 *     ...
 *     "streams_publishes": {
 *         "stream1": {
 *             "type": "message_router",
 *             "dmaap_info": {
 *                 ...
 *             }
 *         },
 *         "stream2": {
 *             "type": "data_router",
 *             "dmaap_info": {
 *                 ...
 *             }
 *         }
 *     },
 *     "streams_subscribes": {
 *         "stream3": {
 *             "type": "message_router",
 *             "dmaap_info": {
 *                 ...
 *             }
 *         },
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class DataStreams {

    private DataStreams() {
    }

    /**
     * <p>
     * Extracts sources from application configuration. Parses <em>streams_subscribes</em> subtree.
     * </p>
     *
     * <p>
     * For sample input it will yield:
     * </p>
     *
     * <pre>
     * [
     *     RawDataStream{
     *         name="stream3"
     *         type="message_router"
     *         direction=SOURCE
     *         descriptor=JsonObject{
     *             type: "message_router",
     *             dmaap_info: {
     *                 ...
     *             }
     *         }
     *     }
     * ]
     * </pre>
     *
     * @param rootJson - the full application configuration
     * @return io.vavr.collection.Stream of data streams
     */
    public static Stream<RawDataStream<JsonObject>> namedSources(JsonObject rootJson) {
        return createCollectionOfStreams(rootJson, DataStreamDirection.SOURCE);
    }


    /**
     * <p>
     * Extracts sinks from application configuration. Parses <em>streams_publishes</em> subtree.
     * </p>
     *
     * <p>
     * For sample input it will yield:
     * </p>
     *
     * <pre>
     * [
     *     RawDataStream{
     *         name="stream1"
     *         type="message_router"
     *         direction=SINK
     *         descriptor=JsonObject{
     *             type: "message_router",
     *             dmaap_info: {
     *                 ...
     *             }
     *         }
     *     },
     *     RawDataStream{
     *         name="stream2"
     *         type="data_router"
     *         direction=SINK
     *         descriptor=JsonObject{
     *             type: "data_router"
     *             dmaap_info: {
     *                 ...
     *             }
     *         }
     *     }
     * ]
     * </pre>
     *
     * @param rootJson - the full application configuration
     * @return io.vavr.collection.Stream of data streams
     */
    public static Stream<RawDataStream<JsonObject>> namedSinks(JsonObject rootJson) {
        return createCollectionOfStreams(rootJson, DataStreamDirection.SINK);
    }

    private static Stream<RawDataStream<JsonObject>> createCollectionOfStreams(JsonObject rootJson, DataStreamDirection direction) {
        final JsonElement streamsJson = rootJson.get(direction.configurationKey());
        return streamsJson == null
                ? Stream.empty()
                : DataStreamUtils.mapJsonToStreams(streamsJson, direction);
    }


}
