/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import static org.assertj.core.api.Assertions.assertThat;

public class CbsClientConfigMapTest {

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @Test
    void shouldFetchUsingProperConfigMapFile() {
        // given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        String expectResult = "{\n" +
                "\t\"keystore.path\": \"/var/run/security/keystore_file.p12\",\n" +
                "\t\"streams_publishes\": {\n" +
                "\t\t\"perf3gpp\": {\n" +
                "\t\t\t\"type\": \"kafka\",\n" +
                "\t\t\t\"aaf_credentials\": {\n" +
                "\t\t\t\t\"username\": \"admin\",\n" +
                "\t\t\t\t\"password\": \"admin_secret\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"kafka_info\": {\n" +
                "\t\t\t\t\"bootstrap_servers\": \"message-router-kafka-0:9093\",\n" +
                "\t\t\t\t\"topic_name\": \"HV_VES_PERF3GPP\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        String configMapFilePath = "src/test/resources/application_config.yaml";
        final CbsClient cut = new CbsClientConfigMap(configMapFilePath);

        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(CbsRequests.getConfiguration(diagnosticContext)).block();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(covertToJson(expectResult));
    }

    private JsonObject covertToJson(String expectResult) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(expectResult, JsonObject.class);
    }
}
