/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.adapters;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiBadArgumentException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiNotFoundException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiPreconditionFailedException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiServiceConnectionException;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import reactor.core.publisher.Mono;

public class AaiExceptions {
    private static AaiException toAaiException(final HttpResponse response) {
        switch (response.statusCode()) {
            case AaiNotFoundException.ERROR_CODE:
                return new AaiNotFoundException();
            case AaiBadArgumentException.ERROR_CODE:
                return new AaiBadArgumentException(response.bodyAsString());
            case AaiPreconditionFailedException.ERROR_CODE:
                return new AaiPreconditionFailedException(response.bodyAsString());
            default:
                return new AaiServiceConnectionException(response.statusCode(), response.bodyAsString());
        }
    }

    public static Mono<HttpResponse> mapToExceptionIfUnsuccessful(final HttpResponse response) {
        return response.successful() ? Mono.just(response) : Mono.error(toAaiException(response));
    }
}
