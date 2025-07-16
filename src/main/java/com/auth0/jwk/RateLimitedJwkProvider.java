package com.auth0.jwk;

/**
 * Jwk provider that limits the amount of Jwks to deliver in a given rate.
 */
@SuppressWarnings("WeakerAccess")
public class RateLimitedJwkProvider implements JwkProvider {

    private final JwkProvider provider;
    private final Bucket bucket;

    /**
     * Creates a new provider that will check the given Bucket if a jwks can be provided now.
     *
     * @param bucket   bucket to limit the amount of jwk requested in a given amount of time.
     * @param provider provider to use to request jwk when the bucket allows it.
     */
    public RateLimitedJwkProvider(JwkProvider provider, Bucket bucket) {
        this.provider = provider;
        this.bucket = bucket;
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        if (!bucket.consume()) {
            throw new RateLimitReachedException(bucket.willLeakIn());
        }
        return provider.get(keyId);
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
