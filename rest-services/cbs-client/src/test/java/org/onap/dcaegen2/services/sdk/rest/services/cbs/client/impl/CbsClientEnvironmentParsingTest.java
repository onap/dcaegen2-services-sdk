/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021-2022 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import java.io.FileReader;
import static org.assertj.core.api.Assertions.assertThat;

public class CbsClientEnvironmentParsingTest {

    private static final String SAMPLE_CONFIG = "src/test/resources/sample_service_config.json";
    private static final String SAMPLE_EXPECTED_CONFIG = "src/test/resources/sample_expected_parsed_service_config.json";
    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @Test
    void shouldProcessEnvironmentVariables() throws FileNotFoundException {
        //given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        JsonObject jsonObject = getSampleJsonObject(SAMPLE_CONFIG);
        //when
        JsonObject result = CbsClientEnvironmentParsing.processEnvironmentVariables(jsonObject);
        //then
        assertThat(result).isEqualTo(getSampleJsonObject(SAMPLE_EXPECTED_CONFIG));
    }

    private JsonObject getSampleJsonObject(String file) throws FileNotFoundException {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader(file));
        return gson.fromJson(reader, JsonObject.class);
    }
}
