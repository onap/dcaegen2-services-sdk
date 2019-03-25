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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.GsonAdaptersAafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class DmaapUtilsTest {

    @Test
    void extractAafCredentials_shouldReturnNull_whenAllFieldsAreNull() {
        // given
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAafCredentials()).create();
        JsonObject json = gson.fromJson("{\"aaf_username\":null,\"aaf_password\":null}", JsonObject.class);

        // when
        final AafCredentials result = DmaapUtils.extractAafCredentials(gson, json);

        // then
        assertThat(result).isNull();
    }

    @Test
    void extractAafCredentials_shouldReturnNull_whenAllFieldsAreAbsent() {
        // given
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAafCredentials()).create();
        JsonObject json = gson.fromJson("{\"whatever\":\"else\"}", JsonObject.class);

        // when
        final AafCredentials result = DmaapUtils.extractAafCredentials(gson, json);

        // then
        assertThat(result).isNull();
    }

    @Test
    void extractAafCredentials_shouldReturnValue_whenBothFieldsAreSet() {
        // given
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAafCredentials()).create();
        JsonObject json = gson.fromJson("{\"aaf_username\":\"uname\",\"aaf_password\":\"passwd\"}", JsonObject.class);

        // when
        final AafCredentials result = DmaapUtils.extractAafCredentials(gson, json);

        // then
        assertThat(result).isEqualTo(ImmutableAafCredentials.builder().username("uname").password("passwd").build());
    }

    @Test
    void extractAafCredentials_shouldReturnValueWithUser_whenOnlyUserIsSet() {
        // given
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAafCredentials()).create();
        JsonObject json = gson.fromJson("{\"aaf_username\":\"uname\"}", JsonObject.class);

        // when
        final AafCredentials result = DmaapUtils.extractAafCredentials(gson, json);

        // then
        assertThat(result).isEqualTo(ImmutableAafCredentials.builder().username("uname").build());
    }

    @Test
    void extractAafCredentials_shouldReturnValueWithUser_whenPasswordIsNull() {
        // given
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAafCredentials()).create();
        JsonObject json = gson.fromJson("{\"aaf_username\":\"uname\",\"aaf_password\":null}", JsonObject.class);

        // when
        final AafCredentials result = DmaapUtils.extractAafCredentials(gson, json);

        // then
        assertThat(result).isEqualTo(ImmutableAafCredentials.builder().username("uname").build());
    }
}