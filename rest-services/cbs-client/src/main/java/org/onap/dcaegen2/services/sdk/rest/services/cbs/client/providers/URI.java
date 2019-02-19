package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers;

class URI {
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

    static final class URIBuilder {
        private String scheme;
        private String host;
        private int port;
        private String path;
        private String fragment;
        private String authority;
        private String userInfo;
        private String query;
        private String schemeSpecificPart;

        URIBuilder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        URIBuilder host(String host) {
            this.host = host;
            return this;
        }

        URIBuilder port(int port) {
            this.port = port;
            return this;
        }

        URIBuilder path(String path) {
            this.path = path;
            return this;
        }

        URIBuilder fragment(String fragment) {
            this.fragment = fragment;
            return this;
        }

        URIBuilder authority(String authority) {
            this.authority = authority;
            return this;
        }

        URIBuilder userInfo(String userInfo) {
            this.userInfo = userInfo;
            return this;
        }

        URIBuilder query(String query) {
            this.query = query;
            return this;
        }

        URIBuilder schemeSpecificPart(String schemeSpecificPart) {
            this.schemeSpecificPart = schemeSpecificPart;
            return this;
        }

        URI build() {
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