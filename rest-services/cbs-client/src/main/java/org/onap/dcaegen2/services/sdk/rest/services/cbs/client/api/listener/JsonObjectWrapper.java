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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.valueOf;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.JsonElementType.JSON_ARRAY;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.JsonElementType.JSON_OBJECT;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.JsonElementType.JSON_PRIMITIVE;

class JsonObjectWrapper {

    private final Map<JsonElementType, HashMap<List<String>, JsonElement>> actualMap;

    JsonObjectWrapper(JsonObject json) {
        actualMap = accumulatePath(List.empty(), vavrHashMap(json)).groupBy(JsonElementType::typeOf);
    }

    private JsonObjectWrapper(Map<JsonElementType, HashMap<List<String>, JsonElement>> map) {
        actualMap = map;
    }

    Option<HashMap<List<String>, JsonElement>> getPrimitivesAsMap() {
        return actualMap.get(JSON_PRIMITIVE);
    }

    JsonObjectWrapper convertJsonObjectsAndArraysToPaths() {
        JsonObjectWrapper tmpWrapper = new JsonObjectWrapper(this.actualMap);
        while (tmpWrapper.containsObjectOrArray()) {
            tmpWrapper = tmpWrapper.reduceLayerOfJsonObjectsAndArrays();
        }
        return tmpWrapper;
    }

    private boolean containsObjectOrArray() {
        return actualMap.containsKey(JsonElementType.JSON_OBJECT) || actualMap.containsKey(JsonElementType.JSON_ARRAY);
    }

    private JsonObjectWrapper reduceLayerOfJsonObjectsAndArrays() {
        Map<JsonElementType, HashMap<List<String>, JsonElement>> actualReducedByJsonObjectsLayer =
                getValuesForType(JSON_OBJECT)
                        .flatMap(m -> m.map(this::splitObjectAccumulatingPath))
                        .getOrElse(HashMap::empty);
        Map<JsonElementType, HashMap<List<String>, JsonElement>> actualReducedByJsonArraysLayer =
                getValuesForType(JSON_ARRAY)
                        .flatMap(m -> m.map(this::splitArrayAccumulatingPath))
                        .getOrElse(HashMap::empty);

        return new JsonObjectWrapper(
                mergeMultiMaps(
                        actualMap.rejectKeys(k -> k == JSON_OBJECT || k == JSON_ARRAY),
                        mergeMultiMaps(actualReducedByJsonObjectsLayer, actualReducedByJsonArraysLayer)));
    }

    private Seq<HashMap<List<String>, JsonElement>> getValuesForType(JsonElementType elementType) {
        return actualMap.filterKeys(elementType::equals).values();
    }

    private Map<JsonElementType, HashMap<List<String>, JsonElement>> splitObjectAccumulatingPath(final Tuple2<List<String>, JsonElement> tuple) {
        final List<String> pathAccumulator = tuple._1;
        final JsonObject jsonObject = tuple._2.getAsJsonObject();
        return accumulatePath(pathAccumulator, vavrHashMap(jsonObject))
                .groupBy(JsonElementType::typeOf);
    }

    private Map<JsonElementType, HashMap<List<String>, JsonElement>> splitArrayAccumulatingPath(final Tuple2<List<String>, JsonElement> tuple) {
        final List<String> pathAccumulator = tuple._1;
        final JsonArray jsonArray = tuple._2.getAsJsonArray();
        return accumulatePath(pathAccumulator, HashMap.ofAll(indexToJsonTuplesStream(jsonArray), Function.identity()))
                .groupBy(JsonElementType::typeOf);
    }

    private Stream<Tuple2<String, JsonElement>> indexToJsonTuplesStream(final JsonArray jsonArray) {
        final AtomicInteger iterator = new AtomicInteger();
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(element -> new Tuple2<>(valueOf(iterator.getAndIncrement()), element));
    }

    private HashMap<List<String>, JsonElement> accumulatePath(final List<String> pathAccumulator,
                                                              final HashMap<String, JsonElement> map) {
        return map.mapKeys(pathAccumulator::append);
    }

    private Map<JsonElementType, HashMap<List<String>, JsonElement>> mergeMultiMaps(
            final Map<JsonElementType, HashMap<List<String>, JsonElement>> thisMap,
            final Map<JsonElementType, HashMap<List<String>, JsonElement>> other) {
        return thisMap.merge(other, HashMap::merge);
    }

    private HashMap<String, JsonElement> vavrHashMap(@NotNull JsonObject json) {
        return HashMap.ofAll(json.entrySet().stream(), this::entryToTuple);
    }

    private Tuple2<String, JsonElement> entryToTuple(final java.util.Map.Entry<String, JsonElement> entry) {
        return new Tuple2<>(entry.getKey(), entry.getValue());
    }
}
