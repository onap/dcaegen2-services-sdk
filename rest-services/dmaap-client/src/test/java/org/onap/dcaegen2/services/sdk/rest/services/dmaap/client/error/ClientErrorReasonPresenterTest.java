/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientErrorReasonPresenterTest {

    @Test
    void shouldSuccessfullyPresent() {
        //given
        ClientErrorReason clientErrorReason = createSimple();
        String expected = "header\n"
                + "{\"requestError\":{\"serviceException\":{\"messageId\":\"messageId\",\"text\":\"text\"}}}";

        //when
        String actual = ClientErrorReasonPresenter.present(clientErrorReason);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldSuccessfullyPresentWithVariables() {
        //given
        ClientErrorReason clientErrorReason = createSimple().withVariables("v1", "v2");
        String expected = "header\n"
                + "{\"requestError\":{\"serviceException\":{\"messageId\":\"messageId\",\"text\":\"text\",\"variables\":[\"v1\",\"v2\"]}}}";

        //when
        String actual = ClientErrorReasonPresenter.present(clientErrorReason);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    private ImmutableClientErrorReason createSimple() {
        return ImmutableClientErrorReason.builder()
                .header("header")
                .messageId("messageId")
                .text("text")
                .build();
    }
}
