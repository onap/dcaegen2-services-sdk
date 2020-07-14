/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import java.security.InvalidParameterException;

public class UrlSplitter {
    public String getUrlPart(String url) {
        String[] urlParts = splitUrl(url);
        return urlParts[0];
    }

    public String getFragmentPart(String url) {
        String[] urlParts = splitUrl(url);
        return urlParts[1];
    }

    private String[] splitUrl(String url) {
        String[] urlParts = url.split("#");
        if (urlParts.length < 2) {
            throw new InvalidParameterException("Couldn't split url. No '# found in: " + url);
        }
        return urlParts;
    }
}
