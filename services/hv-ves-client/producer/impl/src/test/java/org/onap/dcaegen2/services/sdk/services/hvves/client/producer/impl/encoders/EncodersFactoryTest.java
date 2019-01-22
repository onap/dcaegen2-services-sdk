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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jakub.dudycz@nokia.com">Jakub Dudycz</a>
 */
public class EncodersFactoryTest {

    private final EncodersFactory encodersFactory = new EncodersFactory();

    @Test
    public void factory_methods_should_create_non_null_encoders_objects() {
        // when
        final ProtobufEncoder protobufEncoder = encodersFactory.createProtobufEncoder(ByteBufAllocator.DEFAULT);
        final WireFrameEncoder wireFrameEncoder = encodersFactory.createWireFrameEncoder(ByteBufAllocator.DEFAULT);

        // then
        assertThat(protobufEncoder).isNotNull();
        assertThat(wireFrameEncoder).isNotNull();
    }
}
