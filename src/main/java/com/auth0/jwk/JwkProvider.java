package com.auth0.jwk;

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
}
