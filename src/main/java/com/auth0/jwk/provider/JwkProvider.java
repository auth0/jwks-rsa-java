package com.auth0.jwk.provider;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.SigningKeyNotFoundException;

import java.util.List;

public interface JwkProvider {
    List<Jwk> getAll() throws SigningKeyNotFoundException;
    Jwk get(String keyId) throws SigningKeyNotFoundException;
}
