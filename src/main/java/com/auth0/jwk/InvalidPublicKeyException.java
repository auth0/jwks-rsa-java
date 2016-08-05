package com.auth0.jwk;

public class InvalidPublicKeyException extends JwksException {

    public InvalidPublicKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
