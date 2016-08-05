package com.auth0.jwk.provider;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaCachedJwksProvider implements JwkProvider {

    private final Cache<String, Jwk> cache;
    private final JwkProvider provider;

    public GuavaCachedJwksProvider(final JwkProvider provider) {
        this(provider, 5, 10, TimeUnit.HOURS);
    }

    public GuavaCachedJwksProvider(final JwkProvider provider, int size, int expiresIn, TimeUnit expiresUnit) {
        this.provider = provider;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    @Override
    public List<Jwk> getAll() throws SigningKeyNotFoundException {
        return Lists.newArrayList(cache.asMap().values());
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
