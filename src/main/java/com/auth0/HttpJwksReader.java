package com.auth0;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpJwksReader implements JwksReader {
    private final URL url;

    public HttpJwksReader(String jwksUri) {
        if (jwksUri == null || "".equals(jwksUri)) {
            throw new IllegalArgumentException("JwksUri is required");
        }

        try {
            this.url = new URL(jwksUri);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
    }

    public InputStream getJwks() throws SigningKeyNotFoundException {
        try {
            return this.url.openStream();
        } catch (IOException e) {
            throw new SigningKeyNotFoundException("Cannot read public key", e);
        }
    }
}
