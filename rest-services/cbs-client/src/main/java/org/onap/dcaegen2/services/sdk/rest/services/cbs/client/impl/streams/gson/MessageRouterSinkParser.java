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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.javafx.css.CssError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.*;

public class MessageRouterSinkParser implements StreamFromGsonParser<MessageRouterSink> {

    private final Gson gson;

    public static MessageRouterSinkParser create() {
        return new MessageRouterSinkParser(gsonInstance());
    }

    private MessageRouterSinkParser(Gson gson) {
        this.gson = gson;
    }

    public MessageRouterSink unsafeParse(JsonObject input) {
        assertStreamType(input, "message_router");

        Either<StreamParserError, String> aafUsername = Try.of(() -> requiredString(input, "aaf_username"))
                .toEither()
                .mapLeft(StreamParserError::fromThrowable);
        Either<StreamParserError, String> aafPassword = Try.of(() -> requiredString(input, "aaf_password"))
                .toEither()
                .mapLeft(StreamParserError::fromThrowable);

        final JsonElement dmaapInfoJson = requiredChild(input, "dmaap_info");
        final MessageRouterDmaapInfo dmaapInfo = gson.fromJson(dmaapInfoJson, ImmutableMessageRouterDmaapInfo.class);

        return new GsonMessageRouterSink(dmaapInfo, aafUsername.getOrNull(), aafPassword.getOrNull());

    }
}
