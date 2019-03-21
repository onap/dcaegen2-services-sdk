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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class CbsRequestsTest {

    private final RequestDiagnosticContext diagCtx = RequestDiagnosticContext.create();
    private final String serviceName = "srv-name";
    private final EnvProperties env = ImmutableEnvProperties.builder()
            .appName(serviceName)
            .consulHost("consul")
            .consulPort(6666)
            .cbsName("cbs")
            .build();

    @Test
    void getConfiguration() {
        // given
        final CbsRequest cut = CbsRequests.getConfiguration(diagCtx, serviceName);

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/service_component/srv-name");
    }

    @Test
    void getConfigurationEnv() {
        // given
        final CbsRequest cut = CbsRequests.getConfiguration(diagCtx, env);

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/service_component/srv-name");
    }

    @Test
    void getByKey() {
        // given
        final CbsRequest cut = CbsRequests.getByKey(diagCtx, serviceName, "configKey");

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/configKey/srv-name");
    }

    @Test
    void getByKeyEnv() {
        // given
        final CbsRequest cut = CbsRequests.getByKey(diagCtx, env, "configKey");

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/configKey/srv-name");
    }

    @Test
    void getAll() {
        // given
        final CbsRequest cut = CbsRequests.getAll(diagCtx, serviceName);

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/service_component_all/srv-name");
    }

    @Test
    void getAllEnv() {
        // given
        final CbsRequest cut = CbsRequests.getAll(diagCtx, env);

        // when
        final String result = cut.requestPath();

        // then
        assertThat(result).isEqualTo("/service_component_all/srv-name");
    }
}