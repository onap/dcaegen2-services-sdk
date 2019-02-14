package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.ct.api;

import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.ct.DummyCollector;

public interface DummyCollectorFactory {

    @NotNull DummyCollector createCollector();

}