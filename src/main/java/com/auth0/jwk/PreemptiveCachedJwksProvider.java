package com.auth0.jwk;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.annotations.VisibleForTesting;

/**
 * 
 * Caching {@linkplain JwksProvider} which preemptively attempts to update the cache in the background. 
 * The preemptive updates themselves run on a separate, dedicated thread. Updates 
 * are is not continuously scheduled, but (lazily) triggered by incoming requests for JWKs. <br><br>
 * 
 * This class is intended for uninterrupted operation in high-load scenarios, as it will avoid
 * a (potentially) large number of threads blocking when the cache expires (and must be refreshed).<br><br>
 * 
 */

public class PreemptiveCachedJwksProvider extends CachedJwksProvider {

    // preemptive update should execute when 
    // expire - preemptiveTimeout < current time < expire.
    private final long preemptiveTimeout; // milliseconds
    
    private final ReentrantLock lazyLock = new ReentrantLock();

    private final ExecutorService executorService;

    // cache expire time is used as its fingerprint
    private volatile long cacheExpires;

    public PreemptiveCachedJwksProvider(
            JwksProvider provider, 
            long timeToLiveUnits, 
            TimeUnit timeToLiveUnit, 
            long refreshTimeoutUnits, 
            TimeUnit refreshTimeoutUnit, 
            long preemptiveTimeoutUnits, 
            TimeUnit preemptiveTimeoutUnit) {
        this(
                provider, 
                timeToLiveUnit.toMillis(timeToLiveUnits), 
                refreshTimeoutUnit.toMillis(refreshTimeoutUnits), 
                preemptiveTimeoutUnit.toMillis(preemptiveTimeoutUnits),
                Executors.newSingleThreadExecutor()                
                );
    }

    public PreemptiveCachedJwksProvider(JwksProvider provider, long timeToLive, long refreshTimeoutUnits, long preemptiveTimeout) {
        this(provider, timeToLive, refreshTimeoutUnits, preemptiveTimeout, Executors.newSingleThreadExecutor());
    }

    public PreemptiveCachedJwksProvider(JwksProvider provider, long timeToLive, long refreshTimeoutUnits, long preemptiveTimeout, ExecutorService executorService) {
        super(provider, timeToLive, refreshTimeoutUnits);
        
        this.preemptiveTimeout = preemptiveTimeout;
        this.executorService = executorService;
    }

    @VisibleForTesting
    List<Jwk> getJwks(long time, boolean forceUpdate) throws JwkException {
        JwkListCacheItem cache = this.cache;
        if(forceUpdate || cache == null || !cache.isValid(time)) {
            return super.getJwksBlocking(time, cache);
        }
        
        preemptiveUpdate(time, cache);
        
        return cache.getValue();
    }

    /**
     * Preemptive update. 
     * 
     * @param time current time
     * @param cache current cache (non-null)
     */
    
    protected void preemptiveUpdate(long time, JwkListCacheItem cache) {
        if(!cache.isValid(time + preemptiveTimeout)) {
            // cache will expires soon, 
            // preemptively update it
            
            // check if an update is already in progress
            if(cacheExpires < cache.getExpires()) {
                // seems no update is in progress, see if we can get the lock
                if(lazyLock.tryLock()) {
                    try {
                        // check again now that this thread holds the lock
                        if(cacheExpires < cache.getExpires()) {
                            
                            // still no update is in progress
                            cacheExpires = cache.getExpires();
                            
                            // run update in the background
                            executorService.execute(new Runnable() {
                                
                                @Override
                                public void run() {
                                    try {
                                        PreemptiveCachedJwksProvider.super.getJwksBlocking(time, cache);
                                    } catch (JwkException e) {
                                        // update failed, but another thread can retry
                                        cacheExpires = -1L;
                                        // ignore, unable to update
                                        // another thread will attempt the same
                                        // TODO logging?
                                    }
                                }
                            });
                        }
                    } finally {
                        lazyLock.unlock();
                    }
                }
            }
        }
    }

    /**
     * Return the executor service which services the background refresh. 
     * 
     * @return executor service
     */
    
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    protected Jwk getJwtFromCache(String keyId, long time) {
        JwkListCacheItem cache = getCache(time);
        if(cache != null) {
            Jwk jwk = getJwk(keyId, cache.getValue());
            if(jwk != null) {
                preemptiveUpdate(time, cache);
                
                return jwk;
            }
        }
        // no cache or unknown key
        // no preemptive update; caller will do a blocking update.
        return null;
    }
    
    @VisibleForTesting
    ReentrantLock getLazyLock() {
        return lazyLock;
    }
}
