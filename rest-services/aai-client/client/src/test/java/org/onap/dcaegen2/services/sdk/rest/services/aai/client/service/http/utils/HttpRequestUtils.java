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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.utils;

import static io.vavr.Predicates.not;
import static io.vavr.collection.Stream.range;
import static io.vavr.control.Option.ofOptional;
import static java.lang.String.format;
import static java.util.Map.Entry;
import static org.mockito.ArgumentMatchers.argThat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.vavr.control.Option;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.mockito.ArgumentMatcher;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.JsonHelpers;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;


public class HttpRequestUtils {
    private final Gson converter;

    public HttpRequestUtils(Gson converter) {
        this.converter = converter;
    }


    /**
     * Extracts body from HttpRequest, tries to convert it into JsonObject.
     *
     * @param request from which body is obtained.
     * @return none if request's body wasn't convertible to JsonObject or some(JsonObject) otherwise.
     * @throws NullPointerException if request is null.
     */
    public static Option<JsonObject> toJsonObject(HttpRequest request) {
        return Option
                .of(request.body())
                .map(RequestBody::contents)
                .flatMap(Option::of)
                .flatMap(HttpRequestUtils::toJsonObject);
    }

    private static Option<JsonObject> toJsonObject(Publisher<ByteBuf> publisher) {
        return ofOptional(Flux
                .from(publisher)
                .map(buffer -> range(0, buffer.readableBytes()).map(buffer::getByte))
                .flatMap(Flux::fromIterable)
                .collectList()
                .blockOptional()
                .map(HttpRequestUtils::toByteArray)
                .map(String::new)
                .map(new JsonParser()::parse)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject));
    }

    public <T> Option<T> toObject(HttpRequest request, Class<T> type) {
        return toJsonObject(request).map(obj -> converter.fromJson(obj, type));
    }

    /**
     * Extracts body from HttpRequest, tries to convert it into JsonObject and returns Set of its fields.
     *
     * @param request from which body is obtained.
     * @return Set of pairs (FieldName, FieldValue) obtained from JsonObject. Set is empty if conversion failed.
     * @throws NullPointerException if request is null.
     */
    public static Set<Entry<String, JsonElement>> toFieldSet(HttpRequest request) {
        return toJsonObject(request)
                .map(JsonObject::entrySet)
                .getOrElse(Collections.emptySet());
    }

    /**
     * Converts object into JsonObject and returns Set of its fields.
     *
     * @param obj which is being converted into JsonObject.
     * @return Set of pairs (FieldName, FieldValue) obtained from JsonObject. Set is empty if conversion failed.
     */
    public Set<Entry<String, JsonElement>> toFieldSet(Object obj) {
        return JsonHelpers
                .toJsonObject(converter, obj)
                .map(JsonObject::entrySet)
                .getOrElse(Collections.emptySet());
    }

    private static byte[] toByteArray(List<Byte> bytes) {
        final byte[] buffer = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            buffer[i] = bytes.get(i);
        }

        return buffer;
    }

    public static class MatchRequest implements ArgumentMatcher<HttpRequest> {
        private final String[] regexps;
        private final HttpMethod httpMethod;
        private String message = "";

        public MatchRequest(HttpMethod httpMethod, String... regexps) {
            this.regexps = regexps;
            this.httpMethod = httpMethod;
        }

        public static HttpRequest matchReq(HttpMethod httpMethod, String... regexps) {
            return argThat(new MatchRequest(httpMethod, regexps));
        }

        @Override
        public boolean matches(HttpRequest httpRequest) {
            message = "";

            final String url = Option.of(httpRequest.url()).getOrElse("");
            final Option<String> doesntMatch = io.vavr.collection.List.of(regexps).find(not(url::matches));

            if (doesntMatch.isDefined()) {
                message = format("%s doesn't match %s!", httpRequest.url(), doesntMatch.get());

                return false;
            }

            if (httpRequest.method() == null || !httpRequest.method().equals(httpMethod)) {
                message = format("expected %s != actual %s", httpMethod, httpRequest.method());

                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
