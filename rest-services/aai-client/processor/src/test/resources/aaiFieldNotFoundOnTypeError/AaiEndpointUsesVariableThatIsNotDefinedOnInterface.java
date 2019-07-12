package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.test;

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;

@AaiPojo(path = "FOO/${id}/${some_not_existing}", type = "BAR")
public interface AaiEndpointUsesVariableThatIsNotDefinedOnInterface {
    @AaiRequired("id")
    int id();
}