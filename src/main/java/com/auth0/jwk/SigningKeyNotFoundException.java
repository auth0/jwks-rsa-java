package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class SigningKeyNotFoundException extends JwkException {

    public SigningKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
