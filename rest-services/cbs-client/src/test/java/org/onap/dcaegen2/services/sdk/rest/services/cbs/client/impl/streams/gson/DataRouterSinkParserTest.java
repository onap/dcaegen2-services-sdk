///*
// * ============LICENSE_START====================================
// * DCAEGEN2-SERVICES-SDK
// * =========================================================
// * Copyright (C) 2019 Nokia. All rights reserved.
// * =========================================================
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * ============LICENSE_END=====================================
// */
//package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonObject;
//import io.vavr.control.Either;
//import org.junit.jupiter.api.Test;
//import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
//import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamParser;
//import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStream;
//import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//class DataRouterSinkParserTest {
//    private static final String SAMPLE_LOCATION = "mtc00";
//    private static final String SAMPLE_PUBLISH_URL = "https://we-are-data-router.us/feed/xyz";
//    private static final String SAMPLE_LOG_URL = "https://we-are-data-router.us/feed/xyz/logs";
//    private static final String SAMPLE_USER = "some-user";
//    private static final String SAMPLE_PASSWORD = "some-password";
//    private static final String SAMPLE_PUBLISHER_ID = "123456";
//
//    private static final Gson gson = new Gson();
//    private static final String fullConfiguration = "{\n" +
//            "        \"location\": \"" + SAMPLE_LOCATION + "\",\n" +
//            "        \"publish_url\": \"" + SAMPLE_PUBLISH_URL + "\",\n" +
//            "        \"log_url\": \"" + SAMPLE_LOG_URL + "\",\n" +
//            "        \"username\": \"" + SAMPLE_USER + "\",\n" +
//            "        \"password\": \"" + SAMPLE_PASSWORD + "\",\n" +
//            "        \"publisher_id\": \"" + SAMPLE_PUBLISHER_ID + "\"\n" +
//            "}";
//    private static final JsonObject fullJsonObject = gson.fromJson(fullConfiguration, JsonObject.class);
//    private static final DataRouterSink fullConfigurationStream = ImmutableDataRouterSink.builder()
//            .location(SAMPLE_LOCATION)
//            .publishUrl(SAMPLE_PUBLISH_URL)
//            .logUrl(SAMPLE_LOG_URL)
//            .username(SAMPLE_USER)
//            .password(SAMPLE_PASSWORD)
//            .publisherId(SAMPLE_PUBLISHER_ID)
//            .build();
//
//    private static final String minimalConfiguration = "{\n" +
//            "        \"publish_url\": \"" + SAMPLE_PUBLISH_URL + "\"\n" +
//            "}";
//    private static final JsonObject minimalJsonObject = gson.fromJson(minimalConfiguration, JsonObject.class);
//    private static final DataRouterSink minimalConfigurationStream = ImmutableDataRouterSink.builder()
//            .publishUrl(SAMPLE_PUBLISH_URL)
//            .build();
//
//    private static final JsonObject emptyJsonObject = gson.fromJson("{}", JsonObject.class);
//
//    private static Gson parsingGson = new GsonBuilder()
//            .registerTypeAdapterFactory(new GsonAdaptersDataRouterSink())
//            .create();
//    private final StreamParser streamParser = new D;
//
//    @Test
//    void fullConfiguration_shouldGenerateDataRouterSinkObject() {
//        // when
//        Either<StreamParserError, DataStream> result = streamParser.parse(fullJsonObject);
//        // then
//        assertThat(result.get()).isInstanceOf(DataRouterSink.class);
//        assertThat(result.get().equals(fullConfigurationStream)).isTrue();
//    }
//
//    @Test
//    void minimalConfiguration_shouldGenerateDataRouterSinkObject() {
//        // when
//        Either<StreamParserError, DataStream> result = streamParser.parse(minimalJsonObject);
//        // then
//        assertThat(result.get()).isInstanceOf(DataRouterSink.class);
//        assertThat(result.get().equals(minimalConfigurationStream)).isTrue();
//    }
//
//    @Test
//    void emptyConfiguration_shouldParseToStreamParserError() {
//        // when
//        Either<StreamParserError, DataStream> result = streamParser.parse(emptyJsonObject);
//        // then
//        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
//    }
//
//}