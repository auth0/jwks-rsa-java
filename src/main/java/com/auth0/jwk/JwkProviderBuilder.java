package com.auth0.jwk;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * JwkProvider builder
 */
@SuppressWarnings("WeakerAccess")
public class JwkProviderBuilder {

    private final JwkProvider provider;
    private TimeUnit expiresUnit;
    private long expiresIn;
    private long cacheSize;
    private boolean cached;
    private BucketImpl bucket;
    private boolean rateLimited;

    protected JwkProviderBuilder(JwkProvider provider) {
        if (provider == null) {
            throw new IllegalStateException("Cannot build provider without custom provider");
        }
        this.provider = provider;
        this.cached = true;
        this.expiresIn = 10;
        this.expiresUnit = TimeUnit.HOURS;
        this.cacheSize = 5;
        this.rateLimited = true;
        this.bucket = new BucketImpl(10, 1, TimeUnit.MINUTES);
    }

    /**
     * Deprecated in favor of {@link UrlJwkProviderBuilder#from(URL)}.
     *
     * @param url where jwks is published
     * @throws IllegalStateException if domain is null
     */
    @Deprecated
    public JwkProviderBuilder(URL url) {
        this(UrlJwkProviderBuilder.urlJwkProvider(url));
    }

    /**
     * Deprecated in favor of {@link UrlJwkProviderBuilder#from(String)}.
     *
     * @param domain where jwks is published
     * @throws IllegalStateException if domain is null
     */
    @Deprecated
    public JwkProviderBuilder(String domain) {
        this(UrlJwkProviderBuilder.buildJwksUrl(domain));
    }

    /**
     * Creates a new Builder with a custom provider for the jwks.
     *
     * @param provider that will lookup jwks
     * @throws IllegalStateException if provider is null
     * @return the builder
     */
    public static JwkProviderBuilder from(JwkProvider provider) {
        return new JwkProviderBuilder(provider);
    }

    /**
     * Toggle the cache of Jwk. By default the provider will use cache.
     *
     * @param cached if the provider should cache jwks
     * @return the builder
     */
    public JwkProviderBuilder cached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /**
     * Enable the cache specifying size and expire time.
     *
     * @param cacheSize number of jwk to cache
     * @param expiresIn amount of time the jwk will be cached
     * @param unit      unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder cached(long cacheSize, long expiresIn, TimeUnit unit) {
        this.cached = true;
        this.cacheSize = cacheSize;
        this.expiresIn = expiresIn;
        this.expiresUnit = unit;
        return this;
    }

    /**
     * Toggle the rate limit of Jwk. By default the Provider will use rate limit.
     *
     * @param rateLimited if the provider should rate limit jwks
     * @return the builder
     */
    public JwkProviderBuilder rateLimited(boolean rateLimited) {
        this.rateLimited = rateLimited;
        return this;
    }

    /**
     * Enable the cache specifying size and expire time.
     *
     * @param bucketSize max number of jwks to deliver in the given rate.
     * @param refillRate amount of time to wait before a jwk can the jwk will be cached
     * @param unit       unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder rateLimited(long bucketSize, long refillRate, TimeUnit unit) {
        bucket = new BucketImpl(bucketSize, refillRate, unit);
        return this;
    }

    /**
     * Creates a {@link JwkProvider}
     *
     * @return a newly created {@link JwkProvider}
     */
    public JwkProvider build() {
        JwkProvider provider = this.provider;
        if (this.rateLimited) {
            provider = new RateLimitedJwkProvider(provider, bucket);
        }
        if (this.cached) {
            provider = new GuavaCachedJwkProvider(provider, cacheSize, expiresIn, expiresUnit);
        }
        return provider;
    }
}
