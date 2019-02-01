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
        message.readBytes(wpu.wireProtocolHeader);
        wpu.type = message.readShort();
        wpu.payloadSize = message.readInt();
        byte[] payload = new byte[wpu.payloadSize];
        message.readBytes(payload);
        wpu.event = VesEventOuterClass.VesEvent.parseFrom(payload);
        return wpu;
    }
}
