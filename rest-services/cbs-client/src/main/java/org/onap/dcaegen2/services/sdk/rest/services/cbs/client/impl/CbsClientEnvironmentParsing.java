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
import com.google.gson.JsonPrimitive;
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

    private CbsClientEnvironmentParsing() {}

    /**
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
        if (entry.getValue().isJsonArray()) {
            processJsonArray(entry.getValue().getAsJsonArray());
        } else if (entry.getValue().isJsonObject()) {
            processJsonObject(entry.getValue().getAsJsonObject());
        } else {
            Matcher matcher = getMatcher(entry.getValue().getAsString());
            if (matcher.find()) {
                jsonObject.addProperty(entry.getKey(), getValueFromSystemEnv(matcher.group(1)));
            }
        }
    }

    private static void processJsonArray(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).isJsonObject()) {
                processJsonObject(jsonArray.get(i).getAsJsonObject());
            } else if (jsonArray.get(i).isJsonArray()) {
                processJsonArray(jsonArray.get(i).getAsJsonArray());
            } else {
                Matcher matcher = getMatcher(jsonArray.get(i).getAsString());
                if (matcher.find()) {
                    jsonArray.set(i, new JsonPrimitive(getValueFromSystemEnv(matcher.group(1))));
                }
            }
        }
    }

    private static Matcher getMatcher(String value) {
        return shellEnvPattern.matcher(value);
    }

    private static String getValueFromSystemEnv( String envName) {
        String envValue = System.getenv(envName);
        if (envValue == null || "".equals(envValue)) {
            throw new EnvironmentParsingException("Cannot read " + envName + " from environment.");
        }
        return envValue;
    }
}
