/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.security.ssl;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.nio.file.Path;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.1
 */
final class KeyStoreTypes {
    static final String TYPE_JKS = "jks";
    static final String TYPE_PKCS12 = "pkcs12";
    private static final Set<String> JKS_EXTENSIONS = HashSet.of(TYPE_JKS);
    private static final Set<String> PKCS12_EXTENSIONS = HashSet.of(TYPE_PKCS12, "p12");

    private KeyStoreTypes() {}

    static Option<String> guessType(Path filePath) {
        return extension(filePath.toString())
                .flatMap(KeyStoreTypes::typeForExtension);
    }

    private static Option<String> extension(String filePath) {
        final int dotIndex = filePath.lastIndexOf('.');
        return dotIndex < 0 || dotIndex + 1 >= filePath.length()
                ? Option.none()
                : Option.of(filePath.substring(dotIndex + 1).toLowerCase());
    }

    private static Option<String> typeForExtension(String extension) {
        if (JKS_EXTENSIONS.contains(extension)) {
            return Option.of(TYPE_JKS);
        } else if (PKCS12_EXTENSIONS.contains(extension)) {
            return Option.of(TYPE_PKCS12);
        } else {
            return Option.none();
        }
    }
}
