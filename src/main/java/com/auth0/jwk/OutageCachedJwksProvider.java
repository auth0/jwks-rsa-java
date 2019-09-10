package com.auth0.jwk;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This provider implements a workaround for temporary network
 * problems / outage, running into minutes or hours.<br><br>
 * 
 * It transparently caches a delegate {@linkplain JwksProvider}, returning
 * the cached value only when the underlying delegate throws a 
 * {@linkplain SigningKeyUnavailableException}.
 */

public class OutageCachedJwksProvider extends AbstractCachedJwksProvider {

    public OutageCachedJwksProvider(JwksProvider delegate, long timeToLiveUnits, TimeUnit timeToLiveUnit) {
        super(delegate, timeToLiveUnit.toMillis(timeToLiveUnits));
    }

    @Override
    List<Jwk> getJwks(long time) throws JwkException {
        try {
            List<Jwk> all = provider.getJwks();

            // refresh underlying cache
            this.cache = new JwkListCacheItem(all, getExpires(time));

            return all;
        } catch(SigningKeyUnavailableException e1) {
            // attempt to get from underlying cache
            JwkListCacheItem cache = this.cache;
            if(cache != null && cache.isValid(time)) {
                return cache.getValue();
            }

            throw e1;
        }
    }

}
