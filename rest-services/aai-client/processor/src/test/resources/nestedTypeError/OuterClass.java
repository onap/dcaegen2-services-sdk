package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.test;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;

public class OuterClass {

    @AaiPojo(path = "FOO", type = "BAR")
    public interface InnerInterface { }
}