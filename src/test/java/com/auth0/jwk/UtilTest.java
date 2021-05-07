package com.auth0.jwk;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilTest {
    @Test
    public void isNullOrEmpty() {
        assertTrue(Util.isNullOrEmpty(null));
        assertTrue(Util.isNullOrEmpty(""));
        assertFalse(Util.isNullOrEmpty("not empty"));
    }

    @Test
    public void checkArgument() {
        String message = "exception test";
        assertThrows(message, IllegalArgumentException.class, () -> {
            Util.checkArgument(false, message);
        });
        Util.checkArgument(true, message);
    }
}