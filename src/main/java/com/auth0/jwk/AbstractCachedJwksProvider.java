package com.auth0.jwk;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;

/**
 * Jwk provider that caches previously obtained list of Jwk in memory.
 */

public abstract class AbstractCachedJwksProvider extends BaseJwksProvider {

    protected static class JwkListCacheItem {

        private List<Jwk> value;
        private long expires;

        public JwkListCacheItem(List<Jwk> value, long expires) {
            this.value = value;
            this.expires = expires;
        }
        
        public boolean isValid(long time) {
            return time <= expires;
        }

        public List<Jwk> getValue() {
            return value;
        }

        public long getExpires() {
            return expires;
        }
        
    }    
    
    protected volatile JwkListCacheItem cache; 
    protected final long timeToLive; // milliseconds
    
    public AbstractCachedJwksProvider(JwksProvider provider, long timeToLive) {
        super(provider);
        this.timeToLive = timeToLive;
    }

    @VisibleForTesting
    abstract List<Jwk> getJwks(long time) throws JwkException;

    @VisibleForTesting
    long getExpires(long time) {
        return time + timeToLive;
    }
  
    @VisibleForTesting
    long getTimeToLive() {
        return timeToLive;
    }

    @Override
    public List<Jwk> getJwks() throws JwkException {
        return getJwks(System.currentTimeMillis());
    }
    
    protected JwkListCacheItem getCache(long time) {
        JwkListCacheItem cache = this.cache;
        if(cache != null && cache.isValid(time)) {
            return cache;
        }
        return null;
    }
}
