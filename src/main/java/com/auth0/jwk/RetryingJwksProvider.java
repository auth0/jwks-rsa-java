package com.auth0.jwk;

import java.util.List;

/**
 * This provider implements a workaround for transient network problems.
 * <br><br>
 * It retries getting the list of Jwks if the wrapped provider throws
 * a {@linkplain SigningKeyUnavailableException}.
 */

public class RetryingJwksProvider extends BaseJwksProvider {

    public RetryingJwksProvider(JwksProvider provider) {
        super(provider);
    }

    @Override
    public List<Jwk> getJwks() throws JwkException {
        try {
            return provider.getJwks();
        } catch(SigningKeyUnavailableException e) {
            // assume transient network issue, retry once
            return provider.getJwks();
        }
    }
    
}
