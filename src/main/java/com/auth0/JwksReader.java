package com.auth0;

import java.io.InputStream;

public interface JwksReader {
    public InputStream getJwks() throws JwksException;
}
