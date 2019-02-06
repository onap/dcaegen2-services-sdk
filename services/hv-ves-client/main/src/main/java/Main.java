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
import com.google.protobuf.ByteString;
import io.vavr.collection.HashSet;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ProducerOptions;
import org.onap.ves.VesEventOuterClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        ProducerOptions options = ImmutableProducerOptions.builder()
                .collectorAddresses(
                        HashSet.of(new InetSocketAddress("localhost", 6061))
                ).build();


        HvVesProducer producer = HvVesProducerFactory.create(options);

        VesEventOuterClass.VesEvent instance = VesEventOuterClass.VesEvent.newBuilder()
                .setCommonEventHeader(VesEventOuterClass.CommonEventHeader.newBuilder()
                        .setDomain("dummy")
                        .build())
                .setEventFields(ByteString.copyFrom(("                           ").getBytes())
                ).build();

        Mono.from(producer.send(Flux.just(
                instance, instance, instance, instance, instance,
                instance, instance, instance, instance, instance
                ))
        ).block();
        System.exit(0);
    }
}
