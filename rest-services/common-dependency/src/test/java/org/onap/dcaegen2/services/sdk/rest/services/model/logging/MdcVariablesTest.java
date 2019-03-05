package org.onap.dcaegen2.services.sdk.rest.services.model.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MdcVariablesTest {

    @Test
    void shouldReturnProperHttpHeader() {
        String expectedValue = "X-header";
        String returnedValue = MdcVariables.httpHeader("header");

        assertEquals(expectedValue, returnedValue);
    }
}