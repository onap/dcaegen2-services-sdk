/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
 * Copyright (C) 2021 Wipro Limited.
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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import java.io.FileNotFoundException;
import java.io.FileReader;
import static org.assertj.core.api.Assertions.assertThat;

public class CbsClientConfigMapTest {

    private static final String SAMPLE_EXPECTED_CONFIG = "src/test/resources/sample_expected_service_config.json";
    private static final String SAMPLE_EXPECTED_POLICY_CONFIG = "src/test/resources/sample_expected_policy_config.json";
    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @Test
    void shouldFetchUsingProperConfigMapFile() throws FileNotFoundException {
        // given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        String configMapFilePath = "src/test/resources/application_config.yaml";
        String policySyncFilePath = "src/test/resources/policies.json";
        String requestPath = "/service_component/app-name";
        final CbsClient cut = new CbsClientConfigMap(configMapFilePath,policySyncFilePath,requestPath);

        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(CbsRequests.getConfiguration(diagnosticContext)).block();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(convertToJson(new JsonReader(new FileReader(SAMPLE_EXPECTED_CONFIG))));
    }

    @Test
    void shouldFetchUsingConfigMapFileAndPolicySyncFile() throws FileNotFoundException {
        // given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        String configMapFilePath = "src/test/resources/application_config.yaml";
        String policySyncFilePath = "src/test/resources/policies.json";
        String requestPath = "/service_component_all/app-name";
        final CbsClient cut = new CbsClientConfigMap(configMapFilePath,policySyncFilePath,requestPath);

        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(CbsRequests.getConfiguration(diagnosticContext)).block();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(convertToJson(new JsonReader(new FileReader(SAMPLE_EXPECTED_POLICY_CONFIG))));
    }

    private JsonObject convertToJson(JsonReader jsonReader) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonReader, JsonObject.class);
    }
}
