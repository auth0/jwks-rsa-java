package com.auth0;

/**
 * Created by auth0 on 8/4/16.
 */
public class JwksException extends Exception {

    public JwksException(String message) {
        super(message);
    }

    public JwksException(String message, Throwable cause) {
        super(message, cause);
    }

}
