/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.ct;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;
import reactor.util.function.Tuple2;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class DummyCollector {

    private final List<ByteBuf> receivedData = Collections.synchronizedList(new ArrayList<>());
    private DisposableServer server;
    private ReplayProcessor<ClientDisconnected> clientDisconnected = ReplayProcessor.create();
    private Flux<Integer> handledClientsCount = Flux.fromStream(IntStream.iterate(0, x -> x + 1).boxed())
            .zipWith(clientDisconnected)
            .map(Tuple2::getT1)
            .share();

    public InetSocketAddress start() {
        server = TcpServer.create()
                .host("localhost")
                .port(6666)
                .wiretap(true)
                .handle(this::handleConnection)
                .bindNow();
        return server.address();
    }

    public void stop() {
        server.disposeNow();
        server = null;
    }

    public void blockUntilFirstClientIsHandled(Duration timeout) {
        handledClientsCount.blockFirst(timeout);
    }

    public void blockUntilFirstClientsAreHandled(int numClients, Duration timeout) {
        handledClientsCount.take(numClients).blockLast(timeout);
    }

    public ByteBuf dataFromClient(int clientNumber) {
        return receivedData.get(clientNumber);
    }

    public ByteBuf dataFromFirstClient() {
        return dataFromClient(0);
    }

    private Publisher<Void> handleConnection(NettyInbound nettyInbound, NettyOutbound nettyOutbound) {
        nettyInbound.receive()
                .aggregate()
                .retain()
                .log()
                .doOnNext(this::collect)
                .subscribe();

        return nettyOutbound.neverComplete();
    }

    private void collect(ByteBuf buf) {
        receivedData.add(buf);
        clientDisconnected.onNext(ClientDisconnected.INSTANCE);
    }


    private static final class ClientDisconnected {

        private static final ClientDisconnected INSTANCE = new ClientDisconnected();
    }
}
