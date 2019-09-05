package com.auth0.jwk;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RateLimitReachedExceptionTest {
    @Test
    public void shouldGetAvailableIn() throws Exception {
        RateLimitReachedException exception = new RateLimitReachedException(123456789);
        assertThat(exception, notNullValue());
        assertThat(exception.getMessage(), equalTo("The Rate Limit has been reached! Please wait 123456789 milliseconds."));
        assertThat(exception.getAvailableIn(), equalTo(123456789L));
    }

}
