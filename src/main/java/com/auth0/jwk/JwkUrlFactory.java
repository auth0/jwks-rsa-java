package com.auth0.jwk;

import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

class JwkUrlFactory {

    static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

    /**
     * Builds a URL to jwks.json for the given normalized domain.
     * @param domain where jwks is published
     * @return url to jwks
     */
    static URL forNormalizedDomain(String domain) {
        checkArgument(!isNullOrEmpty(domain), "A domain is required");

        try {
            final URL url = new URL(domain);
            return new URL(url, WELL_KNOWN_JWKS_PATH);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
    }
}
