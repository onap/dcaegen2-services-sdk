/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */


package org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils;

import static io.vavr.collection.Stream.ofAll;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.control.Option;

public final class JsonHelpers {
    private JsonHelpers() {
    }

    /**
     * Merges fields from second JsonObject to the first one. If some fields exist in both object then value
     * of the second JsonObject will be used. It doesn't mutate arguments.
     *
     * @param first JsonObject into which fields will be merged.
     * @param second JsonObject from which fields will be merged into `first`.
     * @return `first` with fields copied from `second`.
     * @throws NullPointerException if one of params is null.
     */
    public static JsonObject mergeObjects(JsonObject first, JsonObject second) {
        return ofAll(second.entrySet())
                .foldLeft(first.deepCopy(), (acc, item) -> {
                    acc.add(item.getKey(), item.getValue());

                    return acc;
                });
    }

    /**
     * Ties to convert an object to JsonObject.
     *
     * @param converter used to convert object to JsonElement.
     * @param object is being converted to JsonObject.
     * @param type decides which converter will be used for conversion into JsonElement.
     * @return none if object can't be converted into JsonObject otherwise corresponding some(JsonObject)
     * @throws NullPointerException if one of params is null.
     */
    public static <T> Option<JsonObject> toJsonObject(Gson converter, T object, Class<T> type) {
        return Option
                .of(converter.toJsonTree(object, type))
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject);
    }

    /**
     * Ties to convert an object to JsonObject. Uses object's class value for choosing a converter.
     *
     * @param converter used to convert object to JsonElement.
     * @param object is being converted to JsonObject.
     * @return none if object can't be converted into JsonObject otherwise corresponding some(JsonObject)
     * @throws NullPointerException if one of params is null.
     */
    public static <T> Option<JsonObject> toJsonObject(Gson converter, T object) {
        return Option
                .of(object)
                .map(obj -> converter.toJsonTree(obj, obj.getClass()))
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject);
    }
}
