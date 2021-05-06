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

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.CbsClientCommonException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CbsClientCommon {

    private static final Pattern shellEnvPattern = Pattern.compile("\\$\\{(.+?)}");

    public static void processEnvironmentVariables(JsonObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            if (jsonObject.get(key) instanceof JsonObject) {
                processEnvironmentVariables(jsonObject.get(key).getAsJsonObject());
            } else
            {
                Matcher matcher = shellEnvPattern.matcher(jsonObject.get(key).getAsString());
                if (matcher.find()) {
                    String envName = matcher.group(1);
                    String envValue = System.getenv(envName);
                    if (envValue == null || "".equals(envValue)) {
                        throw new CbsClientCommonException("Cannot read " + envName + " from environment.");
                    }
                    jsonObject.addProperty(key, envValue);
                }
            }
        }
    }
}
