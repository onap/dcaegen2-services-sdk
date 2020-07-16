package org.onap.dcaegen2.services.sdk.services.schemamanager;

import com.fasterxml.jackson.databind.JsonNode;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.FileReader;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.JsonNodeConverter;

import java.io.IOException;
import java.net.URISyntaxException;

public class TestUtils {

    public static String absoluteFilePath(String relativeFilePath) throws URISyntaxException {
        return Validator.class.getClassLoader().getResource(relativeFilePath)
                .toURI().getPath();
    }

    static Validator prepareValidator( String mapperFileName) throws URISyntaxException {
        Validator validator = new ValidatorFactory().create(absoluteFilePath(mapperFileName));
        return validator;
    }

    static JsonNode prepareJsonEvent(String eventFileName) throws URISyntaxException, IOException {
        String jsonEventString = FileReader.readFileAsString(absoluteFilePath(eventFileName));
        JsonNode jsonEvent = JsonNodeConverter.fromString(jsonEventString);
        return jsonEvent;
    }
}
