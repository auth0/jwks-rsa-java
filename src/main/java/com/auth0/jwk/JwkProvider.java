package com.auth0.jwk;

/**
 * Provider of Jwk
 */
public interface JwkProvider {
    /**
     * Returns a jwk using the kid value
     * @param keyId value of kid found in JWT
     * @return a jwk
     * @throws SigningKeyNotFoundException if no jwk can be found using the give kid
     */
    Jwk get(String keyId) throws SigningKeyNotFoundException;
}
