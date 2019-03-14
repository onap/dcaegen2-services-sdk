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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonIOException;
import io.vavr.control.Either;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.DataRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.GsonAdaptersDataRouterSink;

public class DataRouterSinkParser implements StreamParser<DataRouterSink> {
    private Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new GsonAdaptersDataRouterSink())
            .create();

    public Either<StreamParserError, DataRouterSink> parse(JsonObject conf) {
        try {
            return Either.right(gson.fromJson(conf, DataRouterSink.class));
        } catch (JsonSyntaxException | JsonIOException e) {
            return Either.left(new StreamParserError(e.getMessage()));
        }
    }
}
