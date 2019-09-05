package com.auth0.jwk;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;

/**
 * 
 * {@linkplain JwksProvider} that limits the number of invocations per time unit.
 * This guards against frequent, potentially costly, downstream calls.
 * 
 */
@SuppressWarnings("WeakerAccess")
public class RateLimitedJwksProvider extends BaseJwksProvider {

    private final Bucket bucket;

    /**
     * Creates a new provider that will check the given Bucket if a jwks can be provided now.
     *
     * @param bucket   bucket to limit the amount of jwk requested in a given amount of time.
     * @param provider provider to use to request jwk when the bucket allows it.
     */
    public RateLimitedJwksProvider(JwksProvider provider, Bucket bucket) {
        super(provider);
        this.bucket = bucket;
    }

    @Override
    public List<Jwk> getJwks() throws JwkException {
        if (!bucket.consume()) {
            throw new RateLimitReachedException(bucket.willLeakIn());
        }
        return provider.getJwks();
    }
    
    @VisibleForTesting
    Bucket getBucket() {
        return bucket;
    }
}
