package com.auth0.jwk;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.auth0.jwk.UrlJwksProvider.urlForDomain;

/**
 * JwkProvider builder
 */
@SuppressWarnings("WeakerAccess")
public class JwkProviderBuilder {

    // root provider
    private final JwksProvider jwksProvider;

    // cache
    private boolean cached = true;
    private TimeUnit expiresUnit = TimeUnit.HOURS;
    private long expiresIn = 10;
    private TimeUnit refreshExpiresUnit = TimeUnit.SECONDS;
    private long refreshExpiresIn = 15;
    
    private boolean preemptive = false;
    private TimeUnit preemptiveTimeUnit = TimeUnit.SECONDS;
    private long preemptiveTimeUnits = 15;
    
    // rate limiting
    private boolean rateLimited = true;
    private BucketImpl bucket = new BucketImpl(10, 1, TimeUnit.MINUTES);
    
    // retrying
    private boolean retrying = false;

    // shadowed
    private boolean shadowed = false;
    private long shadowedExpiresIn = this.expiresIn * 10;
    private TimeUnit shadowedExpiresUnit = this.expiresUnit;
    
    /**
     * Creates a new Builder with the given URL where to load the jwks from.
     *
     * @param url to load the jwks
     * @throws IllegalArgumentException if url is null
     */
    public JwkProviderBuilder(URL url) {
        this(new UrlJwksProvider(url));
    }
    
    /**
     * Creates a new Builder with the given URL where to load the jwks from.
     *
     * @param url            to load the jwks
     * @param connectTimeout connection timeout in milliseconds (null for default)
     * @param readTimeout    read timeout in milliseconds (null for default)
     * @throws IllegalArgumentException if url is null
     */

    public JwkProviderBuilder(URL url, int connectTimeout, int readTimeout) {
        this(new UrlJwksProvider(url, connectTimeout, readTimeout));
    }

    /**
     * Wrap a specific {@linkplain JwksProvider}. Access to this 
     * instance will be cached and/or rate-limited according to
     * the configuration of this builder.
     *
     * @param jwksProvider       root JwksProvider
     */

    public JwkProviderBuilder(JwksProvider jwksProvider) {
        this.jwksProvider = jwksProvider;
    }
    
    /**
     * Creates a new Builder with a domain where to look for the jwks.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * <br><br> Use {@link #JwkProviderBuilder(URL)} if you need to pass a full URL.
     * @param domain where jwks is published
     * @throws IllegalStateException if domain is null
     * @see UrlJwksProvider#UrlJwksProvider(String)
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
     * @param expiresIn amount of time the jwk will be cached
     * @param unit      unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder cached(long expiresIn, TimeUnit unit) {
        this.cached = true;
        this.expiresIn = expiresIn;
        this.expiresUnit = unit;
        return this;
    }
    
    /**
     * Enable the cache specifying size and expire time.
     *
     * @param expiresIn amount of time the jwk will be cached
     * @param unit      unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder preemptive(long time, TimeUnit unit) {
        this.preemptive = true;
        this.preemptiveTimeUnits = time;
        this.preemptiveTimeUnit = unit;
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
        this.rateLimited = true;
        bucket = new BucketImpl(bucketSize, refillRate, unit);
        return this;
    }

    /**
     * Creates a {@link JwkProvider}
     *
     * @return a newly created {@link JwkProvider}
     */
    public JwkProvider build() {
        if(!this.cached && rateLimited) {
            throw new IllegalStateException("Ratelimiting configured without caching");
        } else if(!this.cached && preemptive) {
            throw new IllegalStateException("Premptive cache refresh configured without caching");
        }
        
        JwksProvider provider = this.jwksProvider;
        if (this.retrying) {
            provider = new RetryingJwksProvider(provider);
        }
        if (this.shadowed) {
            provider = new ShadowCachedJwksProvider(provider, shadowedExpiresIn, shadowedExpiresUnit);
        }
        if (this.rateLimited) {
            provider = new RateLimitedJwksProvider(provider, bucket);
        }
        if(this.preemptive) {
            provider = new PreemptiveCachedJwksProvider(provider, expiresIn, expiresUnit, refreshExpiresIn, refreshExpiresUnit, preemptiveTimeUnits, preemptiveTimeUnit);
        } else if (this.cached) {
            provider = new CachedJwksProvider(provider, expiresIn, expiresUnit, refreshExpiresIn, refreshExpiresUnit);
        }
        if(provider instanceof JwkProvider) {
            return (JwkProvider)provider;
        }
        return new DefaultJwkProvider(provider);
    }
    
    public JwkProviderBuilder retrying(boolean retrying) {
        this.retrying = retrying;
        
        return this;
    }
    
    /**
     * Toggle the shadow cache. By default the Provider will not use a shadow cache.
     *
     * @param shadowed if the shadow cache is enabled
     * @return the builder
     */
    public JwkProviderBuilder shadowed(boolean shadowed) {
        this.shadowed = shadowed;
        return this;
    }
    
    /**
     * Enable the shadow cache specifying size and expire time.
     *
     * @param expiresIn amount of time the jwk will be cached
     * @param unit      unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder shadowed(long expiresIn, TimeUnit unit) {
        this.shadowed = true;
        this.shadowedExpiresIn = expiresIn;
        this.shadowedExpiresUnit = unit;
        return this;
    }
    
}
