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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.collection.Stream;
import java.io.IOException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParsingException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableRawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public final class DataStreamUtils {

    public static Stream<RawDataStream<JsonObject>> mapJsonToStreams(JsonElement streamsJson,
            DataStreamDirection direction) {
        return Stream.ofAll(streamsJson.getAsJsonObject().entrySet())
                .map(namedSinkJson -> {
                    final JsonObject jsonObject = namedSinkJson.getValue().getAsJsonObject();
                    return rawDataStream(namedSinkJson.getKey(), direction, jsonObject);
                });
    }

    public static void assertStreamType(
            RawDataStream<JsonObject> json,
            String expectedType,
            DataStreamDirection expectedDirection) {
        if (!json.type().equals(expectedType)) {
            throw new StreamParsingException(
                    "Invalid stream type. Expected '" + expectedType + "', but was '" + json.type() + "'");
        }
        if (json.direction() != expectedDirection) {
            throw new StreamParsingException(
                    "Invalid stream direction. Expected '" + expectedDirection + "', but was '" + json.direction()
                            + "'");
        }
    }

    public static RawDataStream<JsonObject> readSourceFromResource(String resource) throws IOException {
        return rawDataStream(resource, DataStreamDirection.SOURCE, GsonUtils.readObjectFromResource(resource));
    }

    public  static RawDataStream<JsonObject> readSinkFromResource(String resource) throws IOException {
        return rawDataStream(resource, DataStreamDirection.SINK, GsonUtils.readObjectFromResource(resource));
    }

    private static RawDataStream<JsonObject> rawDataStream(String name, DataStreamDirection direction, JsonObject json) {
        return ImmutableRawDataStream.<JsonObject>builder()
                .name(name)
                .direction(direction)
                .type(GsonUtils.requiredString(json, "type"))
                .descriptor(json)
                .build();
    }
}
