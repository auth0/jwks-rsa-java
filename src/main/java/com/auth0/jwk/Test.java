package com.auth0.jwk;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws Exception {
        // Base provider: Fetches JWKs from a URL
        URL jwkUrl = new URL("https://dev-tanya.us.auth0.com/.well-known/jwks.json");
        JwkProvider urlProvider = new UrlJwkProvider(jwkUrl);

        // Caching provider: Caches up to 10 JWKs for 15 minutes
        JwkProvider cachedProvider = new GuavaCachedJwkProvider(urlProvider, 10, TimeUnit.MINUTES.toMillis(15), TimeUnit.MILLISECONDS);

//        // Rate-limiting provider: Allows up to 5 requests per minute
        Bucket bucket = new BucketImpl(2, 1, TimeUnit.MINUTES);
        JwkProvider rateLimitedProvider = new RateLimitedJwkProvider(cachedProvider, bucket);

        Jwk jwk = null;
        // Use the combined provider to fetch a JWK
        try {
            String keyId = "YMIIjTChX26pqrfPHh";
            jwk = rateLimitedProvider.get(keyId);
            System.out.println("Successfully fetched JWKefef: " + jwk);


            keyId = "IIjTChX26pqrfPHh";
            jwk = rateLimitedProvider.get(keyId);
            System.out.println("Successfully fetched JWK: " + jwk);
        } catch (JwkException e) {
            System.err.println("Failed to fetch JWK: " + e.getMessage());

            String keyId = "_YyDMIIjTChX26pqrfPHh";
            jwk = rateLimitedProvider.get(keyId);
            System.out.println("Catch Block : Successfully fetched JWK: " + jwk);
        }
    }
}
