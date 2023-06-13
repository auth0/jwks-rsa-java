package com.auth0.jwk;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Jwk provider that caches previously obtained Jwk in memory using a Ben Manes' Caffine cache
 */
@SuppressWarnings("WeakerAccess")
public class CaffeineCachedJwkProvider implements JwkProvider {

    private final Cache<String, Jwk> cache;
    private final JwkProvider provider;
    @VisibleForTesting
    static final String NULL_KID_KEY = "null-kid";

    /**
     * Creates a new provider that will cache up to 5 jwks for at most 10 minutes
     *
     * @param provider fallback provider to use when jwk is not cached
     */
    public CaffeineCachedJwkProvider(final JwkProvider provider) {
        this(provider, 5, Duration.ofMinutes(10));
    }

    /**
     * Creates a new cached provider specifying cache size and ttl
     *
     * @param provider    fallback provider to use when jwk is not cached
     * @param size        number of jwk to cache
     * @param expiresIn   amount of time a jwk will live in the cache
     * @param expiresUnit unit of the expiresIn parameter
     */
    public CaffeineCachedJwkProvider(final JwkProvider provider, long size, long expiresIn, TimeUnit expiresUnit) {
        this.provider = provider;
        this.cache = Caffeine.newBuilder()
                .maximumSize(size)
                // configure using timeunit; see https://github.com/auth0/jwks-rsa-java/issues/136
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    /**
     * Creates a new cached provider specifying cache size and ttl
     *
     * @param provider  fallback provider to use when jwk is not cached
     * @param size      number of jwt to cache
     * @param expiresIn amount of time a jwk will live in the cache
     */
    public CaffeineCachedJwkProvider(final JwkProvider provider, long size, Duration expiresIn) {
        this(provider, size, expiresIn.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        try {
            String cacheKey = keyId == null ? NULL_KID_KEY : keyId;
            return cache.get(cacheKey, new Function<String, Jwk>() {
                @Override
                public Jwk apply(String ignored) {
                    try {
                        // key passed to apply function is ignored, as we want to
                        // always use the real keyId here, even if it is null.
                        return provider.get(keyId);
                    } catch (JwkException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Throwable e) {
            // throw the proper exception directly, see https://github.com/auth0/jwks-rsa-java/issues/165
            // cause should always be JwkException, but check just to be safe
            if (e.getCause() instanceof JwkException) {
                throw (JwkException) e.getCause();
            }
            // If somehow cause is not JwkException, just wrap
            throw new JwkException("Unable to obtain key with kid " + keyId, e);
        }
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
