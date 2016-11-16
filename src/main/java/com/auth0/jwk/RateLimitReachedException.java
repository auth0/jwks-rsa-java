package com.auth0.jwk;

@SuppressWarnings("WeakerAccess")
public class RateLimitReachedException extends JwkException {
    private final long availableInMs;

    public RateLimitReachedException(long availableInMs) {
        super(String.format("The Rate Limit has been reached! Please wait %d milliseconds.", availableInMs));
        this.availableInMs = availableInMs;
    }

    /**
     * Returns the delay in which the jwk request can be retried.
     *
     * @return the time to wait in milliseconds before retrying the request.
     */
    public long getAvailableIn() {
        return availableInMs;
    }

}
