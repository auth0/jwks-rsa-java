package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class InvalidPublicKeyException extends JwkException {

    public InvalidPublicKeyException(String msg) {
        super(msg);
    }

    public InvalidPublicKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
