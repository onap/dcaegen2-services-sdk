package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.utlis;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    TEXT_PLAIN("text/plain");

    private String contentType;

    ContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType(){
        return contentType;
    }
}
