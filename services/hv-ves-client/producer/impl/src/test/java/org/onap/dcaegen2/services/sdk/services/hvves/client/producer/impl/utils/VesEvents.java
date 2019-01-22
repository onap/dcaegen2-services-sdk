package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.utils;

import com.google.protobuf.ByteString;
import java.util.UUID;
import org.onap.ves.VesEventOuterClass.CommonEventHeader;
import org.onap.ves.VesEventOuterClass.CommonEventHeader.Priority;
import org.onap.ves.VesEventOuterClass.VesEvent;

public final class VesEvents {

    private VesEvents() {
    }

    public static VesEvent defaultVesEvent() {
        return vesEvent(VesEventDomain.PERF3GPP, UUID.randomUUID().toString(), ByteString.EMPTY);
    }

    public static VesEvent vesEvent(VesEventDomain domain, String id, ByteString eventFields) {
        return vesEvent(commonEventHeader(domain, id), eventFields);
    }

    private static VesEvent vesEvent(CommonEventHeader commonEventHeader, ByteString eventFields) {
        return VesEvent
            .newBuilder()
            .setCommonEventHeader(commonEventHeader)
            .setEventFields(eventFields)
            .build();
    }

    private static CommonEventHeader commonEventHeader(
        VesEventDomain domain,
        String id
    ) {
        return CommonEventHeader.newBuilder()
            .setVersion("sample-version")
            .setDomain(domain.domainName())
            .setSequence(1)
            .setPriority(Priority.NORMAL)
            .setEventId(id)
            .setEventName("sample-event-name")
            .setEventType("sample-event-type")
            .setStartEpochMicrosec(100000000)
            .setLastEpochMicrosec(100000005)
            .setNfNamingCode("sample-nf-naming-code")
            .setNfcNamingCode("sample-nfc-naming-code")
            .setNfVendorName("sample-vendor-name")
            .setReportingEntityId(ByteString.copyFromUtf8("sample-reporting-entity-id"))
            .setReportingEntityName("sample-reporting-entity-name")
            .setSourceId(ByteString.copyFromUtf8("sample-source-id"))
            .setSourceName("sample-source-name")
            .setTimeZoneOffset("+1")
            .setVesEventListenerVersion("7.0.2")
            .build();
    }
}
