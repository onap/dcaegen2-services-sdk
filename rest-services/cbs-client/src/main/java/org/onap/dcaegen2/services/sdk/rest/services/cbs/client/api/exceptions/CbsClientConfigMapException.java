package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions;

import org.jetbrains.annotations.NotNull;

public class CbsClientConfigMapException extends RuntimeException {
    public CbsClientConfigMapException(final @NotNull String message) {
        super(message);
    }
}
