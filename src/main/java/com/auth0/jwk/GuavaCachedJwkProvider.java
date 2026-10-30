package com.auth0.jwk;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Jwk provider that caches previously obtained Jwk in memory using a Google Guava cache
 */
@SuppressWarnings("WeakerAccess")
public class GuavaCachedJwkProvider implements JwkProvider {

    private final Cache<Object, Jwk> cache;
    private final JwkProvider provider;
    private final JwksSource source;
    private final long size;
    private final long expiresInNanos;
    private final Ticker ticker;
    private final Object refreshLock = new Object();
    private volatile long fetchedAt;
    private static final Object NULL_KID_KEY = new Object();

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
        this(provider, null, size, expiresIn, expiresUnit, Ticker.systemTicker());
    }

    /**
     * @param source optional source of whole JWKS responses; when present, every fetch refreshes all cached keys
     *               and they expire together, measured from that fetch
     */
    GuavaCachedJwkProvider(JwkProvider provider, JwksSource source, long size, long expiresIn,
                           TimeUnit expiresUnit, Ticker ticker) {
        Util.checkArgument(expiresIn >= 0, "Invalid expiresIn value '" + expiresIn + "'. Must be a non-negative integer.");
        this.provider = provider;
        this.source = source;
        this.size = size;
        this.expiresInNanos = expiresUnit.toNanos(expiresIn);
        this.ticker = ticker;
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder().maximumSize(size);
        if (source == null) {
            // configure using timeunit; see https://github.com/auth0/jwks-rsa-java/issues/136
            builder.expireAfterWrite(expiresIn, expiresUnit).ticker(ticker);
        }
        this.cache = builder.build();
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
        if (source != null) {
            return getFromSource(keyId);
        }
        try {
            Object cacheKey = keyId == null ? NULL_KID_KEY : keyId;
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

    private Jwk getFromSource(String keyId) throws JwkException {
        Object cacheKey = keyId == null ? NULL_KID_KEY : keyId;
        Jwk cached = getIfFresh(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (refreshLock) {
            cached = getIfFresh(cacheKey);
            if (cached != null) {
                return cached;
            }
            long fetchStartedAt = ticker.read();
            JwkSet jwkSet = source.load();
            retainFrom(jwkSet);
            fetchedAt = fetchStartedAt;
            Jwk jwk = jwkSet.get(keyId);
            cache.put(cacheKey, jwk);
            return jwk;
        }
    }

    private Jwk getIfFresh(Object cacheKey) {
        return ticker.read() - fetchedAt < expiresInNanos ? cache.getIfPresent(cacheKey) : null;
    }

    private void retainFrom(JwkSet jwkSet) {
        for (Object cacheKey : new ArrayList<>(cache.asMap().keySet())) {
            Optional<Jwk> jwk = jwkSet.find(cacheKey == NULL_KID_KEY ? null : (String) cacheKey);
            if (jwk.isPresent()) {
                cache.put(cacheKey, jwk.get());
            } else {
                cache.invalidate(cacheKey);
            }
        }
        if (jwkSet.getKeys().size() <= size) {
            for (Jwk jwk : jwkSet.getKeys()) {
                if (jwk.getId() != null) {
                    cache.asMap().putIfAbsent(jwk.getId(), jwk);
                }
            }
        }
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
