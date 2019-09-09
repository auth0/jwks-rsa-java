package com.auth0.jwk;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

/**
 * Caching {@linkplain JwksProvider}. Blocks when the cache is updated.
 */

public class CachedJwksProvider extends AbstractCachedJwksProvider implements JwkProvider {

    protected final ReentrantLock lock = new ReentrantLock();
    
    protected final long refreshTimeout;

    public CachedJwksProvider(JwksProvider provider, long timeToLiveUnits, TimeUnit timeToLiveUnit, long refreshTimeoutUnits, TimeUnit refreshTimeoutUnit) {
        this(provider, timeToLiveUnit.toMillis(timeToLiveUnits), refreshTimeoutUnit.toMillis(refreshTimeoutUnits));
    }

    public CachedJwksProvider(JwksProvider provider, long timeToLive, long refreshTimeout) {
        super(provider, timeToLive);
        
        this.refreshTimeout = refreshTimeout;
    }

    @VisibleForTesting
    List<Jwk> getJwks(long time, boolean forceUpdate) throws JwkException {
        JwkListCacheItem cache = this.cache;
        if(forceUpdate || cache == null || !cache.isValid(time)) {
            return getJwksBlocking(time, cache);
        }
        
        return cache.getValue();
    }

    protected List<Jwk> getJwksBlocking(long time, JwkListCacheItem cache) throws JwkException, SigningKeyUnavailableException {
        // Synchronize so that the first thread to acquire the lock
        // exclusively gets to call the underlying provider.
        // Other (later) threads must wait until the result is ready.
        //
        // If the first to get the lock fails within the waiting interval,
        // subsequent threads will attempt to update the cache themselves.
        //
        // This approach potentially blocks a number of threads,
        // but requesting the same data downstream is not better, so
        // this is a necessary evil.
        
        try {
            if(lock.tryLock(refreshTimeout, TimeUnit.MILLISECONDS)) {
                // see if anyone already refreshed the cache while we were 
                // hold getting the lock
                if(cache == this.cache) {
                    // Seems cache was not updated. 
                    // We hold the lock, so safe to update it now
                    try {
                        List<Jwk> all = provider.getJwks();
    
                        // save to cache
                        this.cache = cache = new JwkListCacheItem(all, getExpires(time));
                    } finally {
                        lock.unlock();
                    }
                } else {
                    // load updated value
                    cache = this.cache;
                    
                }
            } else {
                throw new SigningKeyUnavailableException("Timeout while waiting for refreshed cache (limit of " + refreshTimeout + "ms exceed).");
            }
            
            if(cache != null && cache.isValid(time)) {
                return cache.getValue();
            }
            
            throw new SigningKeyUnavailableException("Unable to refresh cache");
        } catch (InterruptedException e) {
            throw new SigningKeyUnavailableException("Interrupted while waiting for refreshed cache", e);
        }
    }
    
    @Override
    public Jwk getJwk(String keyId) throws JwkException {
        return getJwk(keyId, System.currentTimeMillis());
    }

    Jwk getJwk(String keyId, long time) throws JwkException {
        Jwk jwk = getJwtFromCache(keyId, time);
        if(jwk != null) {
            return jwk;
        }
        // no cache, or an unknown key id
        // refresh the cache
        jwk = getJwk(keyId, getJwks(time, true));
        if(jwk != null) {
            return jwk;
        }

        throw new SigningKeyNotFoundException("No key found for key id " + keyId);
    }

    protected Jwk getJwtFromCache(String keyId, long time) {
        JwkListCacheItem cache = getCache(time);
        if(cache != null) {
            return getJwk(keyId, cache.getValue());
        }
        return null;
    }
    
    protected Jwk getJwk(String keyId, List<Jwk> jwks) {
        for (Jwk jwk : jwks) {
            if (Objects.equals(keyId, jwk.getId())) {
                return jwk;
            }
        } 
        return null;
    }

    @Override
    List<Jwk> getJwks(long time) throws JwkException {
        return getJwks(time, false);
    }

    @VisibleForTesting
    ReentrantLock getLock() {
        return lock;
    }
}
