package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlSplitterTest {

    private static UrlSplitter urlSplitter;

    @BeforeAll
    static void setUp() {
        urlSplitter = new UrlSplitter();
    }

    @Test
    public void shouldReturnFirstPartOfLocalUrl() {
        //given
        String url = "http://localhost:8080/test#secondPartOfUrl";
        String expectedPart = "http://localhost:8080/test";

        //when
        String actualPart = urlSplitter.getUrlPart(url);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnSecondPartOfLocalUrl() {
        //given
        String url = "http://localhost:8080/test#secondPartOfUrl";
        String expectedPart = "secondPartOfUrl";

        //when
        String actualPart = urlSplitter.getFragmentPart(url);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingSecondPartOfUrlAndAddressDoesNotContainHash() {
        //given
        String invalidUrlAddress = "urlWithoutHash";

        //when
        String result = UrlSplitterTest.urlSplitter.getFragmentPart(invalidUrlAddress);

        //then
        assertEquals("", result);
    }
}