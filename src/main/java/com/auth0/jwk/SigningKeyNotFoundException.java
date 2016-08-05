package com.auth0.jwk;

public class SigningKeyNotFoundException extends JwksException {

    public SigningKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
