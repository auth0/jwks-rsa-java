package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class InvalidPrivateKeyException extends JwkException {

    public InvalidPrivateKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
