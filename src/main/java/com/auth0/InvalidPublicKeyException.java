package com.auth0;

public class InvalidPublicKeyException extends JwksException {

    public InvalidPublicKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
