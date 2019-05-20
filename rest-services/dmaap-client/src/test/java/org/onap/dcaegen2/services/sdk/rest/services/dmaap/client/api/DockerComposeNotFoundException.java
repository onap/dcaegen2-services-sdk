package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

class DockerComposeNotFoundException extends RuntimeException{
    DockerComposeNotFoundException(String s){
        super(s);
    }
}
