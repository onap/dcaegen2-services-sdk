/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2020-2021 Nokia. All rights reserved.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.model.ClientError;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.model.ImmutableClientError;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.model.ImmutableRequestError;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.model.ImmutableServiceException;

public class ClientErrorReasonPresenter {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String PATTERN = "%s\n%s";

    public String present(ClientErrorReason clientErrorReason) {
        ImmutableServiceException simpleServiceException = ImmutableServiceException.builder()
                .messageId(clientErrorReason.messageId())
                .text(clientErrorReason.text())
                .build();
        ImmutableServiceException serviceException = Option.of(clientErrorReason.variables())
                .map(simpleServiceException::withVariables)
                .getOrElse(simpleServiceException);
        ImmutableRequestError requestError = ImmutableRequestError.builder()
                .serviceException(serviceException)
                .build();
        ClientError clientError = ImmutableClientError.builder()
                .requestError(requestError)
                .build();
        return String.format(PATTERN, clientErrorReason.header(), GSON.toJson(clientError));
    }
}
