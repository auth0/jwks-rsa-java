package com.auth0.jwk;

class Util {
    static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    static void checkArgument(boolean arg, String message) {
        if (!arg) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }
}
