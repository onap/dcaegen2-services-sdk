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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTopicCredentialsTest {

    @Test
    void shouldCreateBasicAuthHeader() {
        // given
        SecureTopicCredentials credentials = create("username", "password");

        // when
        Tuple2<String, String> basicAuthHeader = credentials.basicAuthHeader();

        // then
        verifyBasicAuthHeader(basicAuthHeader, "dXNlcm5hbWU6cGFzc3dvcmQ=");
    }

    @Test
    void shouldCreateBasicAuthHeaderForEmpties() {
        // given
        SecureTopicCredentials credentials = create("", "");

        // when
        Tuple2<String, String> basicAuthHeader = credentials.basicAuthHeader();

        // then
        verifyBasicAuthHeader(basicAuthHeader, "Og==");
    }

    private SecureTopicCredentials create(String username, String password) {
        return ImmutableSecureTopicCredentials.builder()
                .username(username)
                .password(password.toCharArray())
                .build();
    }

    private void verifyBasicAuthHeader(Tuple2<String, String> basicAuthHeader, String encoded) {
        Tuple2<String, String> expected = Tuple.of("Authorization", "Basic " + encoded);
        assertThat(basicAuthHeader).isEqualTo(expected);
    }
}
