package com.auth0;

/**
 * Created by auth0 on 8/4/16.
 */
public class SigningKeyNotFoundException extends JwksException {

    public SigningKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SigningKeyNotFoundException(String msg) {
        super(msg);
    }
}
