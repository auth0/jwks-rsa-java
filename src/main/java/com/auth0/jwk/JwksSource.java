package com.auth0.jwk;

/**
 * Loads the keys of the current JWKS.
 */
interface JwksSource {

    JwkSet load() throws JwkException;
}
