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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.mr.GsonAdaptersMessageRouterDmaapInfo;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka.GsonAdaptersKafkaInfo;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.GsonAdaptersAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.GsonAdaptersDataRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.GsonAdaptersDataRouterSource;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public final class GsonUtils {

    private static final Lazy<Gson> GSON = Lazy.of(() -> {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersKafkaInfo());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersAafCredentials());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersMessageRouterDmaapInfo());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersDataRouterSink());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersDataRouterSource());
        return gsonBuilder.create();
    });

    private GsonUtils() {
    }

    public  static Gson gsonInstance() {
        return GSON.get();
    }

    public  static String requiredString(JsonObject parent, String childName) {
        return requiredChild(parent, childName).getAsString();
    }

    public  static Option<String> optionalString(JsonObject parent, String childName) {
            return Option.of(parent.get(childName).getAsString());
    }

    public static JsonElement requiredChild(JsonObject parent, String childName) {
        return optionalChild(parent, childName)
                .getOrElseThrow(() -> new IllegalArgumentException(
                        "Could not find sub-node '" + childName + "'. Actual sub-nodes: "
                                + stringifyChildrenNames(parent)));

    }

    public  static Option<JsonElement> optionalChild(JsonObject parent, String childName) {
        return Option.of(parent.get(childName));
    }

    public  static JsonObject readObjectFromResource(String resource) throws IOException {
        return readFromResource(resource).getAsJsonObject();
    }

    public static JsonElement readFromResource(String resource) throws IOException {
        try (Reader reader = new InputStreamReader(GsonUtils.class.getResourceAsStream(resource))) {
            return new JsonParser().parse(reader);
        }
    }

    private static String stringifyChildrenNames(JsonObject parent) {
        return parent.entrySet().stream().map(Entry::getKey).collect(Collectors.joining(", "));
    }
}
