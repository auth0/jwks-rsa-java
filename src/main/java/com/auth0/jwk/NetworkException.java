package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class NetworkException extends SigningKeyNotFoundException {

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
