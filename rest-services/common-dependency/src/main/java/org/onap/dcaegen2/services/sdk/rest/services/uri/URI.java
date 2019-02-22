/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
 *
 */

package org.onap.dcaegen2.services.sdk.rest.services.uri;

public final class URI {
    private String scheme;
    private String host;
    private int port;
    private String path;
    private String fragment;
    private String authority;
    private String userInfo;
    private String query;
    private String schemeSpecificPart;
    private String string;

    private URI() {
    }

    public static final class URIBuilder {
        private String scheme;
        private String host;
        private int port;
        private String path;
        private String fragment;
        private String authority;
        private String userInfo;
        private String query;
        private String schemeSpecificPart;

        public URIBuilder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public URIBuilder host(String host) {
            this.host = host;
            return this;
        }

        public URIBuilder port(int port) {
            this.port = port;
            return this;
        }

        public URIBuilder path(String path) {
            this.path = path;
            return this;
        }

        public URIBuilder fragment(String fragment) {
            this.fragment = fragment;
            return this;
        }

        public URIBuilder authority(String authority) {
            this.authority = authority;
            return this;
        }

        public URIBuilder userInfo(String userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        public URIBuilder query(String query) {
            this.query = query;
            return this;
        }

        public URIBuilder schemeSpecificPart(String schemeSpecificPart) {
            this.schemeSpecificPart = schemeSpecificPart;
            return this;
        }

        public URI build() {
            URI uri = new URI();
            uri.scheme = this.scheme;
            uri.host = this.host;
            uri.port = this.port;
            uri.path = this.path;
            uri.fragment = this.fragment;
            uri.authority = this.authority;
            uri.userInfo = this.userInfo;
            uri.query = this.query;
            uri.schemeSpecificPart = this.schemeSpecificPart;
            return uri;
        }
    }

    @Override
    public String toString() {
        defineString();
        return string;
    }

    private void defineString() {
        if (string != null) return;

        StringBuffer sb = new StringBuffer();
        if (scheme != null) {
            sb.append(scheme);
            sb.append(':');
        }
        if (isOpaque()) {
            sb.append(schemeSpecificPart);
        } else {
            if (host != null) {
                sb.append("//");
                if (userInfo != null) {
                    sb.append(userInfo);
                    sb.append('@');
                }
                boolean needBrackets = ((host.indexOf(':') >= 0)
                    && !host.startsWith("[")
                    && !host.endsWith("]"));
                if (needBrackets) sb.append('[');
                sb.append(host);
                if (needBrackets) sb.append(']');
                if (port != -1) {
                    sb.append(':');
                    sb.append(port);
                }
            } else if (authority != null) {
                sb.append("//");
                sb.append(authority);
            }
            if (path != null)
                sb.append(path);
            if (query != null) {
                sb.append('?');
                sb.append(query);
            }
        }
        if (fragment != null) {
            sb.append('#');
            sb.append(fragment);
        }
        string = sb.toString();
    }

    private boolean isOpaque() {
        return path == null;
    }
}