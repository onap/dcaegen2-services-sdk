package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;

class UrlSplitterTest {

    private static UrlSplitter urlSplitter;

    @BeforeAll
    static void setUp() {
        urlSplitter = new UrlSplitter();
    }

    @Test
    public void shouldReturnFirstPartOfLocalUrl() {

        //given
        String validUrlAddress = "firstPartOfUrl#secondPartOfUrl";
        String expectedPart = "firstPartOfUrl";

        //when
        String actualPart = urlSplitter.getUrlPart(validUrlAddress);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnSecondPartOfLocalUrl() {

        //given
        String validUrlAddress = "firstPartOfUrl#secondPartOfUrl";
        String expectedPart = "secondPartOfUrl";

        //when
        String actualPart = urlSplitter.getFragmentPart(validUrlAddress);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnEmptyStringWhenAddressDoesNotContainHash() {

        //given
        String invalidUrlAddress = "urlWithoutHash";

        //when
        String result = UrlSplitterTest.urlSplitter.getFragmentPart(invalidUrlAddress);

        //then
        assertEquals("", result);
    }

}