package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.test;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;

@AaiPojo(path = "FOO/${baz}", type = "BAR")
public interface InterfaceWithSingleRequiredField {
    @AaiRequired("baz")
    int id();
}