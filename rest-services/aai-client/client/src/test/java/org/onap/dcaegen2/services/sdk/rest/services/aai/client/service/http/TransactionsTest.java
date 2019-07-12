/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.Transaction.Action.PUT;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class TransactionsTest {

    @Test
    void shouldBuildTransactionRequest() throws IOException {
        // given
        JsonObject payload = new JsonObject();
        payload.addProperty("link-name", "foo");

        ImmutableTransactions transactions = ImmutableTransactions
            .builder()
            .addOperations(ImmutableTransaction.builder()
                .action(PUT)
                .uri("/network/logical-links/logical-link/foo")
                .body(payload)
                .build())
            .build();

        // when
        Gson gson = new Gson().newBuilder().create();
        String transaction = gson.toJson(transactions);

        // then
        String expectedJson = getJsonFromFile("transaction.json");
        assertThat(transaction).isEqualToIgnoringWhitespace(expectedJson);
    }

    private String getJsonFromFile(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(TransactionsTest.class.getClassLoader().getResourceAsStream(file))))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}