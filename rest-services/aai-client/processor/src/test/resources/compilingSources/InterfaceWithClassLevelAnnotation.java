package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.test;

import org.junit.jupiter.api.TestInstance;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;

@AaiPojo(path = "FOO/${baz}", type = "BAR")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface InterfaceWithClassLevelAnnotation {
    @AaiRequired("baz")
    int id();
}