package com.auth0.jwk;

/**
 * Token Bucket interface.
 */
interface Bucket {

    /**
     * Calculates the wait time before one token will be available in the Bucket.
     *
     * @return the wait time in milliseconds in which one token will be available in the Bucket.
     */
    long willLeakIn();

    /**
     * Calculates the wait time before the given amount of tokens will be available in the Bucket.
     *
     * @return the wait time in milliseconds in which the given amount of tokens will be available in the Bucket.
     */
    long willLeakIn(int count);

    /**
     * Tries to consume one token from the Bucket.
     *
     * @return true if it could consume the token or false if the Bucket doesn't have tokens available now.
     */
    boolean consume();

    /**
     * Tries to consume the given amount of tokens from the Bucket.
     *
     * @return true if it could consume the given amount of tokens or false if the Bucket doesn't have that amount of tokens available now.
     */
    boolean consume(int count);
}
