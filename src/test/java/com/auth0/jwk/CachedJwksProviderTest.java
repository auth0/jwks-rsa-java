package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class CachedJwksProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";

    private CachedJwksProvider provider;

    @Mock
    private JwksProvider fallback;

    @Mock
    private Jwk jwk;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private List<Jwk> jwks;

    @Before
    public void setUp() throws Exception {
        provider = new CachedJwksProvider(fallback, 10, TimeUnit.HOURS, 15, TimeUnit.SECONDS);
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

}

