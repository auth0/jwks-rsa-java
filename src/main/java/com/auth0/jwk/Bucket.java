package com.auth0.jwk;

interface Bucket {
    long willLeakIn();

    long willLeakIn(int count);

    boolean consume();

    boolean consume(int count);
}
