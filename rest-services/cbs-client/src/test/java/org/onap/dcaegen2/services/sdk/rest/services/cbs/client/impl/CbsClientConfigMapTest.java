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
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.service.FileReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CbsClientConfigMapTest {
    private final RxHttpClient httpClient = mock(RxHttpClient.class);

    @Test
    void shouldFetchUsingProperConfigMapFile() {
        // given
        String configMapFilePath = "src/test/resources/application_config.yaml";
        final CbsClient cut = new CbsClientConfigMap(configMapFilePath);

        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(CbsRequests.getConfiguration(diagnosticContext)).block();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(new Gson().fromJson(new FileReader(configMapFilePath).getContent(), JsonObject.class));
    }
}
