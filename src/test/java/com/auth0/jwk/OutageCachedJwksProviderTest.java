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
public class OutageCachedJwksProviderTest {

    private OutageCachedJwksProvider provider;

    @Mock
    private JwksProvider fallback;

    @Mock
    private Jwk jwk;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private List<Jwk> jwks;

    @Before
    public void setUp() throws Exception {
        provider = new OutageCachedJwksProvider(fallback, 10, TimeUnit.HOURS);

        jwks = Arrays.asList(jwk);
    }

    @Test
    public void shouldUseFallback() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks);
        assertThat(provider.getJwks(), equalTo(jwks));
    }

    @Test
    public void shouldUseFallbackWhenCached() throws Exception {
        List<Jwk> last = Arrays.asList(jwk, jwk);

        when(fallback.getJwks()).thenReturn(jwks).thenReturn(last);
        assertThat(provider.getJwks(), equalTo(jwks));
        assertThat(provider.getJwks(), equalTo(last));
    }

    @Test
    public void shouldUseCacheWhenFallbackSigningKeyUnavailable() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks).thenThrow(new SigningKeyUnavailableException("TEST!", null));
        provider.getJwks();
        assertThat(provider.getJwks(), equalTo(jwks));
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldNotUseExpiredCacheWhenFallbackSigningKeyUnavailable() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks).thenThrow(new SigningKeyUnavailableException("TEST!", null));
        provider.getJwks();

        expectedException.expect(SigningKeyUnavailableException.class);

        provider.getJwks(provider.getExpires(System.currentTimeMillis() + 1));
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }
}
