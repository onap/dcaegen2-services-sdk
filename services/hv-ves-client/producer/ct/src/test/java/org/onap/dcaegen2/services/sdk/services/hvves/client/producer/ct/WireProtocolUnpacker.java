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

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import org.onap.ves.VesEventOuterClass;

public class WireProtocolUnpacker {
    byte[] wireProtocolHeader;
    short type;
    int payloadSize;
    VesEventOuterClass.VesEvent event;

    public static WireProtocolUnpacker unpack(ByteBuf message) throws InvalidProtocolBufferException {
        WireProtocolUnpacker wpu = new WireProtocolUnpacker();
        wpu.wireProtocolHeader = new byte[6];
        message.readBytes(wpu.wireProtocolHeader);
        wpu.type = message.readShort();
        wpu.payloadSize = message.readInt();
        byte[] payload = new byte[wpu.payloadSize];
        message.readBytes(payload);
        wpu.event = VesEventOuterClass.VesEvent.parseFrom(payload);
        return wpu;
    }
}
