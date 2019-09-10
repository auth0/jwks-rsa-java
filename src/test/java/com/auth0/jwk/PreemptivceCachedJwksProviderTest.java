package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.auth0.jwk.AbstractCachedJwksProvider.JwkListCacheItem;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class PreemptivceCachedJwksProviderTest {

    private Runnable lockRunnable = new Runnable() {
        @Override
        public void run() {
            if(!provider.getLazyLock().tryLock()) {
                throw new RuntimeException();
            }
            System.out.println("Got lock");
        }
    };
    
    private Runnable unlockRunnable = new Runnable() {
        @Override
        public void run() {
            provider.getLazyLock().unlock();
            System.out.println("Released lock");
        }
    }; 
    
    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";

    private PreemptiveCachedJwksProvider provider;

    @Mock
    private JwksProvider fallback;

    @Mock
    private Jwk jwk;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private List<Jwk> jwks;

    @Before
    public void setUp() throws Exception {
        provider = new PreemptiveCachedJwksProvider(fallback, 10, TimeUnit.HOURS, 15, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
        when(jwk.getId()).thenReturn(KID);
        jwks = Arrays.asList(jwk);
    }

    @Test
    public void shouldUseFallbackWhenNotCached() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks);
        assertThat(provider.getJwks(), equalTo(jwks));
    }

    @Test
    public void shouldUseCachedValue() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.getJwks();
        assertThat(provider.getJwks(), equalTo(jwks));
        verify(fallback, only()).getJwks();
    }
    
    @Test
    public void shouldUseFallbackWhenExpiredCache() throws Exception {
    
        List<Jwk> first = Arrays.asList(jwk);
        List<Jwk> second = Arrays.asList(jwk, jwk);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first
        provider.getJwks(); 
        assertThat(provider.getJwks(), equalTo(first));
        verify(fallback, only()).getJwks();

        // second
        provider.getJwks(provider.getExpires(System.currentTimeMillis() + 1)); 
        assertThat(provider.getJwks(), equalTo(second));
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldNotReturnExpiredValueWhenExpiredCache() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.getJwks();
        assertThat(provider.getJwks(), equalTo(jwks));
    
        expectedException.expect(SigningKeyNotFoundException.class);
        provider.getJwks(provider.getExpires(System.currentTimeMillis() + 1)); 
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }

    @Test
    public void shouldUseCachedValueForKnownKey() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.getJwk(KID);
        assertThat(provider.getJwk(KID), equalTo(jwk));
        verify(fallback, only()).getJwks();
    }

    @Test
    public void shouldRefreshCacheForUncachedKnownKey() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        // second
        assertThat(provider.getJwk("b"), equalTo(b));
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldRefreshCacheAndThrowExceptionForUnknownKey() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        // second
        expectedException.expect(SigningKeyNotFoundException.class);
        provider.getJwk("c");
    }
    
    @Test
    public void shouldPreemptivelyRefreshCacheForKey() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        assertThat(provider.getJwk("a", provider.getExpires(System.currentTimeMillis()) - TimeUnit.SECONDS.toMillis(5)), equalTo(a));

        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
        verify(fallback, times(2)).getJwks();
        
        // second
        assertThat(provider.getJwk("b"), equalTo(b));
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldPreemptivelyRefreshCacheForKeys() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first jwks
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        long justBeforeExpiry = provider.getExpires(System.currentTimeMillis()) - TimeUnit.SECONDS.toMillis(5);

        assertThat(provider.getJwks(justBeforeExpiry), equalTo(first)); // triggers a preemptive refresh attempt

        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
        verify(fallback, times(2)).getJwks();
        
        // second jwks
        assertThat(provider.getJwk("b"), equalTo(b)); // should already be in cache
        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS); // just to make sure
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldNotPreemptivelyRefreshCacheIfRefreshAlreadyInProgress() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);
    
        when(fallback.getJwks()).thenReturn(first).thenReturn(second);

        // first jwks
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        JwkListCacheItem cache = provider.getCache(System.currentTimeMillis());
        
        long justBeforeExpiry = provider.getExpires(System.currentTimeMillis()) - TimeUnit.SECONDS.toMillis(5);
        
        assertThat(provider.getJwks(justBeforeExpiry), equalTo(first)); // triggers a preemptive refresh attempt

        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
        
        provider.preemptiveUpdate(justBeforeExpiry, cache); // should not trigger a preemptive refresh attempt
        
        verify(fallback, times(2)).getJwks();
        
        // second jwks
        assertThat(provider.getJwk("b"), equalTo(b)); // should already be in cache
        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS); // just to make sure
        verify(fallback, times(2)).getJwks();
    }
    
    @Test
    public void shouldFirePreemptivelyRefreshCacheAgainIfPreviousPreemptivelyRefreshAttemptFailed() throws Exception {
        Jwk a = mock(Jwk.class);
        when(a.getId()).thenReturn("a");
        Jwk b = mock(Jwk.class);
        when(b.getId()).thenReturn("b");
        
        List<Jwk> first = Arrays.asList(a);
        List<Jwk> second = Arrays.asList(b);

        when(fallback.getJwks()).thenReturn(first).thenThrow(new SigningKeyUnavailableException("TEST!")).thenReturn(second);

        // first jwks
        assertThat(provider.getJwk("a"), equalTo(a));
        verify(fallback, only()).getJwks();

        long justBeforeExpiry = provider.getExpires(System.currentTimeMillis()) - TimeUnit.SECONDS.toMillis(5);
        
        assertThat(provider.getJwks(justBeforeExpiry), equalTo(first)); // triggers a preemptive refresh attempt

        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);
        
        assertThat(provider.getJwks(justBeforeExpiry), equalTo(first)); // triggers a another preemptive refresh attempt

        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS);

        verify(fallback, times(3)).getJwks();
        
        // second jwks
        assertThat(provider.getJwk("b"), equalTo(b)); // should already be in cache
        provider.getExecutorService().awaitTermination(1, TimeUnit.SECONDS); // just to make sure
        verify(fallback, times(3)).getJwks();
    }
    
    @Test
    public void shouldAccceptIfAnotherThreadPreemptivelyUpdatesCache() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks);

        provider.getJwks();

        long justBeforeExpiry = provider.getExpires(System.currentTimeMillis()) - TimeUnit.SECONDS.toMillis(5);

        ThreadHelper helper = new ThreadHelper().addRun(lockRunnable).addPause().addRun(unlockRunnable);
        try {
            helper.begin();

            provider.getJwks(justBeforeExpiry); // wants to update, but can't get lock

            verify(fallback, only()).getJwks();
        } finally {
            helper.close();
        }
    }    

   
}

