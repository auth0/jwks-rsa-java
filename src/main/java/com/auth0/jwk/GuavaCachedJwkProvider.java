package com.auth0.jwk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Jwk provider that caches previously obtained Jwk in memory using a Google Guava cache
 */
@SuppressWarnings("WeakerAccess")
public class GuavaCachedJwkProvider implements JwkProvider {

    private final Cache<String, Jwk> cache;
    private final JwkProvider provider;
    @VisibleForTesting
    static final String NULL_KID_KEY = "null-kid";

    /**
     * Creates a new provider that will cache up to 5 jwks for at most 10 minutes
     *
     * @param provider fallback provider to use when jwk is not cached
     */
    public GuavaCachedJwkProvider(final JwkProvider provider) {
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
    public GuavaCachedJwkProvider(final JwkProvider provider, long size, long expiresIn, TimeUnit expiresUnit) {
        this.provider = provider;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                // configure using timeunit; see https://github.com/auth0/jwks-rsa-java/issues/136
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    /**
     * Creates a new cached provider specifying cache size and ttl
     *
     * @param provider    fallback provider to use when jwk is not cached
     * @param size        number of jwt to cache
     * @param expiresIn   amount of time a jwk will live in the cache
     */
    public GuavaCachedJwkProvider(final JwkProvider provider, long size, Duration expiresIn) {
        this(provider, size, expiresIn.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        try {
            String cacheKey = keyId == null ? NULL_KID_KEY : keyId;
            return cache.get(cacheKey, () -> provider.get(keyId));
        } catch (ExecutionException e) {
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
