package com.auth0.jwk;

import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * The keys from a single JWKS response.
 */
final class JwkSet {

    private final URL url;
    private final List<Jwk> keys;

    JwkSet(URL url, List<Jwk> keys) {
        this.url = url;
        this.keys = keys;
    }

    List<Jwk> getKeys() {
        return keys;
    }

    Optional<Jwk> find(String keyId) {
        if (keyId == null && keys.size() == 1) {
            return Optional.of(keys.get(0));
        }
        if (keyId != null) {
            for (Jwk jwk : keys) {
                if (keyId.equals(jwk.getId())) {
                    return Optional.of(jwk);
                }
            }
        }
        return Optional.empty();
    }

    Jwk get(String keyId) throws SigningKeyNotFoundException {
        return find(keyId).orElseThrow(() ->
                new SigningKeyNotFoundException("No key found in " + url.toString() + " with kid " + keyId, null)
        );
    }
}
