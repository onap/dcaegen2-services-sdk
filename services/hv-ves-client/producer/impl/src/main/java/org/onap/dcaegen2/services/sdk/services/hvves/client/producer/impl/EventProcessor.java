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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
    private final EmitterProcessor<ByteBuf> processor;
    private AtomicReference<Channel> channel = new AtomicReference<>();
    private AtomicInteger eventHandledCounter = new AtomicInteger(0);

    public EventProcessor() {
        processor = EmitterProcessor.create();
    }

    void setChannel(Channel channel) {
        this.channel.set(channel);
    }

    void addEvent(ByteBuf byteBuf) {
        processor.onNext(byteBuf);
    }

    void startProcessingAndDoOnComplete(Runnable doOnComplete) {
        int eventsToHandle = processor.getPending();
        LOGGER.info("Processor will handle " + eventsToHandle + " items");
        processor
                .doOnComplete(doOnComplete)
                .doOnNext(it -> {
                    waitSome();
                    if (!channel.get().isOpen()) {
                        throw new ChannelClosedException("Channel closed.");
                    } else {
                        channel.get().writeAndFlush(it);
                        int currentMessage = eventHandledCounter.incrementAndGet();
                        LOGGER.info("Handled event no: " + currentMessage);
                        if (currentMessage == eventsToHandle) {
                            LOGGER.info("Processed last item, completing");
                            processor.onComplete();
                        }
                    }
                })
                .onErrorContinue((cause, msg) -> {
                    if (cause instanceof ChannelClosedException) {
                        LOGGER.debug("Failed to process message: " + cause.getLocalizedMessage());
                        processor.onNext((ByteBuf) msg);
                    } else {
                        throw (RuntimeException) cause;
                    }
                })
                .subscribe();
    }

    private void waitSome() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            LOGGER.error("tyfy-tyfy", e);
        }
    }
}
