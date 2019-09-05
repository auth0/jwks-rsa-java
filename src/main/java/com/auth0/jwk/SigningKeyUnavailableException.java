package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class SigningKeyUnavailableException extends JwkException {

    public SigningKeyUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SigningKeyUnavailableException(String message) {
        super(message);
    }
}
