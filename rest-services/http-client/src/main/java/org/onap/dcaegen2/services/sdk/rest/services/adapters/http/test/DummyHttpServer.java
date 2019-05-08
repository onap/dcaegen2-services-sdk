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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test;

import io.vavr.CheckedFunction0;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
public class DummyHttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyHttpServer.class);
    private final DisposableServer server;

    private DummyHttpServer(DisposableServer server) {
        this.server = server;
    }

    public static DummyHttpServer start(Consumer<HttpServerRoutes> routes) {
        LOGGER.info("Starting dummy server");
        final DisposableServer server = HttpServer.create()
                .host("127.0.0.1")
                .route(routes)
                .bind()
                .block();
        LOGGER.info("Server started");
        return new DummyHttpServer(server);
    }

    public static Publisher<Void> sendInOrder(AtomicInteger state, Publisher<Void>... responses) {
        return responses[state.getAndIncrement()];
    }

    public static Publisher<Void> sendResource(HttpServerResponse httpServerResponse, String resourcePath) {
        return sendString(httpServerResponse, Mono.fromCallable(() -> readResource(resourcePath)));
    }

    public static Publisher<Void> sendString(HttpServerResponse httpServerResponse, Publisher<String> content) {
        return httpServerResponse.sendString(content);
    }

    public void close() {
        server.disposeNow();
    }

    public String host() {
        return server.host();
    }

    public int port() {
        return server.port();
    }

    private static String readResource(String resourcePath) {
        try {
            return CheckedFunction0.constant(resourcePath)
                    .andThen(DummyHttpServer.class::getResource)
                    .andThen(URL::toURI)
                    .andThen(Paths::get)
                    .andThen(Files::readAllBytes)
                    .andThen(String::new)
                    .apply();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
