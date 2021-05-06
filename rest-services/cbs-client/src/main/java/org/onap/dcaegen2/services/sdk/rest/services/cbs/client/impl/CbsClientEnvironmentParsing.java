/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.EnvironmentParsingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Config Binding Service client environment variables parsing.</p>
 *
 * @since 1.8.4
 */
public class CbsClientEnvironmentParsing {

    private static final Pattern shellEnvPattern = Pattern.compile("\\$\\{(.+?)}");

    /**
     * <p>Process</p>
     *
     * <p>
     * This method will do a lookup of shell variables in provided jsonObject and replace it with found environment variables.
     * </p>
     * <p>
     * In case of failure during resolving environment variables, EnvironmentParsingException is thrown.
     * </p>
     *
     * @param jsonObject
     * @return JsonObject
     * @since 1.8.4
     */
    public static JsonObject processEnvironmentVariables(JsonObject jsonObject) {
        JsonObject jsonObjectCopy = jsonObject.deepCopy();
        processJsonObject(jsonObjectCopy);
        return jsonObjectCopy;
    }
    private static void processJsonObject(JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            processJsonObjectEntry(jsonObject, entry);
        }
    }
    private static void processJsonObjectEntry(JsonObject jsonObject, Map.Entry<String, JsonElement> entry) {
        if (entry.getValue() instanceof JsonArray) {
            for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                processJsonObject(jsonElement.getAsJsonObject());
            }
        } else if (entry.getValue() instanceof JsonObject) {
            processJsonObject(entry.getValue().getAsJsonObject());
        } else {
            processJsonPrimitive(jsonObject, entry);
        }
    }
    private static void processJsonPrimitive(JsonObject jsonObject, Map.Entry<String, JsonElement> entry) {
        Matcher matcher = shellEnvPattern.matcher(entry.getValue().getAsString());
        if (matcher.find()) {
            String envName = matcher.group(1);
            String envValue = System.getenv(envName);
            if (envValue == null || "".equals(envValue)) {
                throw new EnvironmentParsingException("Cannot read " + envName + " from environment.");
            }
            jsonObject.addProperty(entry.getKey(), envValue);
        }
    }
}
