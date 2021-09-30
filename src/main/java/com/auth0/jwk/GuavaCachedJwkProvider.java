package com.auth0.jwk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
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
        this(provider, 5, 10, TimeUnit.MINUTES);
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
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        try {
            String cacheKey = keyId == null ? NULL_KID_KEY : keyId;
            return cache.get(cacheKey, new Callable<Jwk>() {
                @Override
                public Jwk call() throws Exception {
                    return provider.get(keyId);
                }
            });
        } catch (ExecutionException e) {
            throw new SigningKeyNotFoundException("Failed to get key with kid " + keyId, e);
        }
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
