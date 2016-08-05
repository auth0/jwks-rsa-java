package com.auth0.jwk;

public class JwksException extends Exception {

    public JwksException(String message) {
        super(message);
    }

    public JwksException(String message, Throwable cause) {
        super(message, cause);
    }

}
