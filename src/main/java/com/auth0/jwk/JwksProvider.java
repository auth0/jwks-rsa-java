package com.auth0.jwk;

import java.util.List;

/**
 * Provider of a list of {@linkplain Jwk}.
 */
public interface JwksProvider {
    
    /**
     * Returns a list of {@linkplain Jwk}.
     * 
     * @return a list of {@linkplain Jwk}
     * @throws JwkException if no list can be retrieved
     */
    List<Jwk> getJwks() throws JwkException;    
}
