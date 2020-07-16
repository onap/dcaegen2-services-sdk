package org.onap.dcaegen2.services.sdk.services.schemamanager;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.onap.dcaegen2.services.sdk.services.schemamanager.TestUtils.prepareValidator;
import static org.onap.dcaegen2.services.sdk.services.schemamanager.TestUtils.prepareJsonEvent;

public class ValidatorTest {

    private static final String MAPPER_DIRECTORY = "externalRepo/schema-map.json";
    private static final String EVENTS_DIRECTORY = "events/";


    @Test
    public void shouldReturnTrueWhenJsonEventIsValid() throws Exception {
        //given
        Validator validator = prepareValidator(MAPPER_DIRECTORY);
        JsonNode jsonEvent = prepareJsonEvent(EVENTS_DIRECTORY + "ves7_valid_eventWithStndDefinedFields.json");
        //when
        Boolean validationResult = validator.validate(jsonEvent);
        //then
        assertThat(validationResult).isTrue();
    }
    @Test
    public void shouldReturnFalseWhenJsonEventIsInvalid() throws Exception {
        //given
        Validator validator = prepareValidator(MAPPER_DIRECTORY);
        JsonNode jsonEvent = prepareJsonEvent(EVENTS_DIRECTORY + "ves7_invalid_eventWithStndDefinedFields.json");
        //when
        Boolean validationResult = validator.validate(jsonEvent);
        //then
        assertThat(validationResult).isFalse();
    }
}
