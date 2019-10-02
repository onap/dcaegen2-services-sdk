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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.api;

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.POST;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;

/**
 * @see Transaction
 * @see <a href="https://docs.onap.org/en/casablanca/submodules/aai/aai-common.git/docs/AAI%20REST%20API%20Documentation/bulkApi.html">AAI
 * Bulk API</a>
 */
@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface Transactions extends Request {

    String BULK_SINGLE_TRANSACTION = "/bulk/single-transaction";

    @SerializedName("operations")
    List<Transaction> operations();

    default HttpMethod method() {
        return POST;
    }

    default String uri() {
        return BULK_SINGLE_TRANSACTION;
    }
}