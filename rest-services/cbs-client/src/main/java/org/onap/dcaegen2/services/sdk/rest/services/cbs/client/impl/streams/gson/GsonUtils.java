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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vavr.Lazy;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.GsonAdaptersAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableRawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
final class GsonUtils {

    private static final Lazy<Gson> GSON = Lazy.of(() -> {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersKafkaInfo());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersAafCredentials());
        return gsonBuilder.create();
    });

    private GsonUtils() {
    }

    static Gson gsonInstance() {
        return GSON.get();
    }

    static void assertStreamType(
            RawDataStream<JsonObject> json,
            String expectedType,
            DataStreamDirection expectedDirection) {
        final String actualType = requiredString(json.descriptor(), "type");
        if (!actualType.equals(expectedType)) {
            throw new IllegalArgumentException(
                    "Invalid stream type. Expected '" + expectedType + "', but was '" + actualType + "'");
        }
        if (json.direction() != expectedDirection) {
            throw new IllegalArgumentException(
                    "Invalid stream direction. Expected '" + expectedDirection + "', but was '" + json.direction()
                            + "'");
        }
    }

    static String requiredString(JsonObject parent, String childName) {
        return requiredChild(parent, childName).getAsString();
    }

    static JsonElement requiredChild(JsonObject parent, String childName) {
        return optionalChild(parent, childName)
                .getOrElseThrow(() -> new IllegalArgumentException(
                        "Could not find sub-node '" + childName + "'. Actual sub-nodes: "
                                + stringifyChildrenNames(parent)));

    }

    static Option<JsonElement> optionalChild(JsonObject parent, String childName) {
        if (parent.has(childName)) {
            return Option.of(parent.get(childName));
        } else {
            return Option.none();
        }
    }

    static RawDataStream<JsonObject> readDataSourceFromResource(String resource) throws IOException {
        return rawDataStream(resource, DataStreamDirection.SOURCE, readObjectFromResource(resource));
    }

    static RawDataStream<JsonObject> readDataSinkFromResource(String resource) throws IOException {
        return rawDataStream(resource, DataStreamDirection.SINK, readObjectFromResource(resource));
    }

    static JsonObject readObjectFromResource(String resource) throws IOException {
        return readFromResource(resource).getAsJsonObject();
    }

    static JsonElement readFromResource(String resource) throws IOException {
        try (Reader reader = new InputStreamReader(GsonUtils.class.getResourceAsStream(resource))) {
            return new JsonParser().parse(reader);
        }
    }

    private static String stringifyChildrenNames(JsonObject parent) {
        return parent.entrySet().stream().map(Entry::getKey).collect(Collectors.joining(", "));
    }

    private static RawDataStream<JsonObject> rawDataStream(String name, DataStreamDirection direction, JsonObject json) {
        return ImmutableRawDataStream.<JsonObject>builder()
                .name(name)
                .direction(direction)
                .descriptor(json)
                .build();
    }
}
