package com.auth0.jwk;

import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.auth0.jwk.UrlJwkProvider.urlForDomain;

/**
 * JwkProvider builder
 */
@SuppressWarnings("WeakerAccess")
public class JwkProviderBuilder {

    private final URL url;
    private Proxy proxy;
    private Duration expiresIn;
    private Integer connectTimeout;
    private Integer readTimeout;
    private long cacheSize;
    private boolean cached;
    private BucketImpl bucket;
    private boolean rateLimited;
    private Map<String, String> headers;

    /**
     * Creates a new Builder with the given URL where to load the jwks from.
     *
     * @param url to load the jwks
     * @throws IllegalStateException if url is null
     */
    public JwkProviderBuilder(URL url) {
        if (url == null) {
            throw new IllegalStateException("Cannot build provider without url to jwks");
        }
        this.url = url;
        this.cached = true;
        this.expiresIn = Duration.ofHours(10);
        this.cacheSize = 5;
        this.rateLimited = true;
        this.bucket = new BucketImpl(10, 1, TimeUnit.MINUTES);
    }

    /**
     * Creates a new Builder with a domain where to look for the jwks.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * <br><br> Use {@link #JwkProviderBuilder(URL)} if you need to pass a full URL.
     *
     * @param domain where jwks is published
     * @throws IllegalStateException if domain is null
     * @see UrlJwkProvider#UrlJwkProvider(String)
     */
    public JwkProviderBuilder(String domain) {
        this(buildJwkUrl(domain));
    }

    private static URL buildJwkUrl(String domain) {
        if (domain == null) {
            throw new IllegalStateException("Cannot build provider without domain");
        }
        return urlForDomain(domain);
    }

    /**
     * Toggle the cache of Jwk. By default, the provider will use a cache size of 5 and a duration of 10 hours.
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
     * @return the builder
     */
    public JwkProviderBuilder cached(long cacheSize, Duration expiresIn) {
        this.cached = true;
        this.cacheSize = cacheSize;
        this.expiresIn = expiresIn;
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
        return this.cached(cacheSize, Duration.ofSeconds(unit.toSeconds(expiresIn)));
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
     * Sets the proxy to use for the connection.
     *
     * @param proxy proxy server to use when making this connection
     * @return the builder
     */
    public JwkProviderBuilder proxied(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Sets a custom connect and read timeout values.
     * When this method is not called, the default timeout values
     * will be those defined by the {@link UrlJwkProvider} implementation.
     *
     * @param connectTimeout connection timeout in milliseconds.
     * @param readTimeout read timeout in milliseconds.
     * @see UrlJwkProvider
     * @return the builder
     */
    public JwkProviderBuilder timeouts(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the headers to send on the request. Any headers set here will override the default headers ("Accept": "application/json")
     *
     * @param headers a map of header keys to values to send on the request.
     * @return this builder instance
     */
    public JwkProviderBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Creates a {@link JwkProvider}
     *
     * @return a newly created {@link JwkProvider}
     */
    public JwkProvider build() {
        JwkProvider urlProvider = new UrlJwkProvider(url, connectTimeout, readTimeout, proxy, headers);
        if (this.rateLimited) {
            urlProvider = new RateLimitedJwkProvider(urlProvider, bucket);
        }
        if (this.cached) {
            urlProvider = new GuavaCachedJwkProvider(urlProvider, cacheSize, expiresIn);
        }
        return urlProvider;
    }
}
