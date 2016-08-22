package com.auth0.jwk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaCachedJwkProvider implements JwkProvider {

    private final Cache<String, Jwk> cache;
    private final JwkProvider provider;

    public GuavaCachedJwkProvider(final JwkProvider provider) {
        this(provider, 5, 10, TimeUnit.HOURS);
    }

    public GuavaCachedJwkProvider(final JwkProvider provider, int size, int expiresIn, TimeUnit expiresUnit) {
        this.provider = provider;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    @Override
    public Jwk get(final String keyId) throws SigningKeyNotFoundException {
        try {
            return cache.get(keyId, new Callable<Jwk>() {
                @Override
                public Jwk call() throws Exception {
                    return provider.get(keyId);
                }
            });
        } catch (ExecutionException e) {
            throw new SigningKeyNotFoundException("Failed to get key with kid " + keyId, e);
        }
    }
}
