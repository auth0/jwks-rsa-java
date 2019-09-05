package com.auth0.jwk;

import java.util.List;
import java.util.Objects;

import com.google.common.annotations.VisibleForTesting;

/**
 * Jwk provider extracts a key from an underlying {@linkplain JwksProvider}.
 */
@SuppressWarnings("WeakerAccess")
public class DefaultJwkProvider implements JwkProvider {

    private final JwksProvider provider;
    
    /**
     * Creates a new provider.
     *
     * @param provider source of jwks.
     */
    public DefaultJwkProvider(final JwksProvider provider) {
        this.provider = provider;
    }

    @Override
    public Jwk getJwk(final String keyId) throws JwkException {
        final List<Jwk> jwks = provider.getJwks();
        for (Jwk jwk : jwks) {
            if (Objects.equals(keyId, jwk.getId())) {
                return jwk;
            }
        }
        throw new SigningKeyNotFoundException("No key found for key id " + keyId);
    }

    @VisibleForTesting
    JwksProvider getBaseProvider() {
        return provider;
    }

}
