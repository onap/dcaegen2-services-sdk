package org.onap.dcaegen2.services.sdk.services.schemamanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.onap.dcaegen2.services.sdk.services.schemamanager.TestUtils.prepareValidator;
import static org.onap.dcaegen2.services.sdk.services.schemamanager.TestUtils.prepareJsonEvent;

public class ValidatorTest {

    private static final String MAPPER_DIRECTORY = "externalRepo/";
    private static final String VALID_MAPPER = MAPPER_DIRECTORY + "valid-schema-map.json";
    private static final String EVENTS_DIRECTORY = "events/";
    public static final String VES_7_VALID_JSON_EVENT = EVENTS_DIRECTORY + "ves7_valid_eventWithStndDefinedFields.json";


    @Test
    public void shouldReturnTrueWhenJsonEventIsValid() throws Exception {
        //given
        Validator validator = prepareValidator(VALID_MAPPER);
        JsonNode jsonEvent = prepareJsonEvent( VES_7_VALID_JSON_EVENT);
        //when
        Boolean validationResult = validator.validate(jsonEvent);
        //then
        assertThat(validationResult).isTrue();
    }
    @Test
    public void shouldReturnFalseWhenJsonEventIsInvalid() throws Exception {
        //given
        Validator validator = prepareValidator(VALID_MAPPER);
        JsonNode jsonEvent = prepareJsonEvent(EVENTS_DIRECTORY + "ves7_invalid_eventWithStndDefinedFields.json");
        //when
        Boolean validationResult = validator.validate(jsonEvent);
        //then
        assertThat(validationResult).isFalse();
    }
    @Test
    public void shouldThrowExceptionWhenKeyInMapperIsWrong() {
        assertThatThrownBy(() -> {
            prepareValidator(MAPPER_DIRECTORY + "invalid-schema-map-wrong-key.json");
        }).isInstanceOf(Exception.class);
    }

    @Test
    public void shouldThrowExceptionWhenKeyInMapperIsMissing() {
        assertThatThrownBy(() -> {
            prepareValidator(MAPPER_DIRECTORY + "invalid-schema-map-missing-key.json");
        }).isInstanceOf(Exception.class);
    }

    @Test
    public void shouldThrowExceptionWhenLocalResourceInMapperIsMissing() {
        assertThatThrownBy(() -> {
            prepareValidator(MAPPER_DIRECTORY + "invalid-schema-map-missing-local-resource.json");
        }).isInstanceOf(Exception.class);
    }

    @Test
    public void shouldThrowExceptionWhenLocalResourceInMapperIsEmpty() {
        assertThatThrownBy(() -> {
            prepareValidator(MAPPER_DIRECTORY + "invalid-schema-map-missing-local-empty.json");
        }).isInstanceOf(Exception.class);
    }

}
