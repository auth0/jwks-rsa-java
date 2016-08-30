package com.auth0.jwk;

import java.util.concurrent.TimeUnit;

/**
 * JwkProvider builder
 */
@SuppressWarnings("WeakerAccess")
public class JwkProviderBuilder {

    private String url;
    private TimeUnit expiresUnit;
    private long expiresIn;
    private long cacheSize;
    private boolean cached;

    /**
     * Creates a new builder
     */
    public JwkProviderBuilder() {
        this.cached = true;
        this.expiresIn = 10;
        this.expiresUnit = TimeUnit.HOURS;
        this.cacheSize = 5;
    }

    /**
     * Specifies the URL from where to load the jwks. It can be a url lik 'https://samples.auth0.com'
     * or just the Auth0 domain 'samples.auth0.com'.
     * @param domain from where to load the jwks
     * @return the builder
     */
    public JwkProviderBuilder forDomain(String domain) {
        this.url = normalizeDomain(domain);
        return this;
    }

    /**
     * Toggle the cache of Jwk
     * @param cached if the provider should cache jwks
     * @return the builder
     */
    public JwkProviderBuilder cached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /**
     * Enable the cache specifying size and expire time.
     * @param cacheSize number of jwk to cache
     * @param expiresIn amount of time the jwk will be cached
     * @param unit unit of time for the expire of jwk
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
     * Creates a {@link JwkProvider}
     * @return a newly created {@link JwkProvider}
     */
    public JwkProvider build() {
        if (url == null) {
            throw new IllegalStateException("Cannot build provider without domain");
        }

        final UrlJwkProvider urlProvider = new UrlJwkProvider(url);
        if (!this.cached) {
            return urlProvider;
        }

        return new GuavaCachedJwkProvider(urlProvider, cacheSize, expiresIn, expiresUnit);
    }

    private String normalizeDomain(String domain) {
        if (!domain.startsWith("http")) {
            return "https://" + domain;
        }
        return domain;
    }
}
