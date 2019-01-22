package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.utils;

public enum VesEventDomain {
    FAULT("fault"),
    HEARTBEAT("heartbeat"),
    MEASUREMENT("measurement"),
    MOBILE_FLOW("mobileFlow"),
    OTHER("other"),
    PNF_REGISTRATION("pnfRegistration"),
    SIP_SIGNALING("sipSignaling"),
    STATE_CHANGE("stateChange"),
    SYSLOG("syslog"),
    THRESHOLD_CROSSING_ALERT("thresholdCrossingAlert"),
    VOICE_QUALITY("voiceQuality"),
    PERF3GPP("perf3gpp");

    private String name;

    VesEventDomain(String name) {
        this.name = name;
    }

    public String domainName() {
        return name;
    }
}
