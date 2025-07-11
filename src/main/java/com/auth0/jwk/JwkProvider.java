package com.auth0.jwk;

import java.util.List;

/**
 * Provider of Jwk
 */
@SuppressWarnings("WeakerAccess")
public interface JwkProvider {
    /**
     * Attempts to get a JWK using the Key ID value. Note that implementations are synchronous (blocking).
     *
     * @param keyId value of the kid found in a JWT
     * @return a JWK
     * @throws SigningKeyNotFoundException if no jwk can be found using the given kid
     */
    Jwk get(String keyId) throws JwkException;

    /**
     * Fetches all available JWKs. Note that implementations are synchronous (blocking).
     *
     * @return a list of all JWKs
     * @throws JwkException if unable to fetch or parse the JWKs
     */
    default List<Jwk> getAll() throws JwkException {
        throw new UnsupportedOperationException("Fetching all JWKs is not supported by this provider.");
    }
}
