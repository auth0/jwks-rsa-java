package com.auth0.jwk;

import com.google.common.annotations.VisibleForTesting;

public abstract class BaseJwksProvider implements JwksProvider {

    protected final JwksProvider provider;

    public BaseJwksProvider(JwksProvider provider) {
        this.provider = provider;
    }
    
    @VisibleForTesting
    JwksProvider getBaseProvider() {
        return provider;
    }    
}
