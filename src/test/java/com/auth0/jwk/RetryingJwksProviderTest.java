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

@RunWith(MockitoJUnitRunner.class)
public class RetryingJwksProviderTest {

    private RetryingJwksProvider provider;

    @Mock
    private JwksProvider fallback;

    @Mock
    private Jwk jwk;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private List<Jwk> jwks;

    @Before
    public void setUp() throws Exception {
        provider = new RetryingJwksProvider(fallback);
        
        jwks = Arrays.asList(jwk);
    }

    @Test
    public void shouldReturnListOnSuccess() throws Exception {
        when(fallback.getJwks()).thenReturn(jwks);
        assertThat(provider.getJwks(), equalTo(jwks));
        verify(fallback, times(1)).getJwks();
    }

    @Test
    public void shouldRetryWhenUnavailable() throws Exception {
        when(fallback.getJwks()).thenThrow(new SigningKeyUnavailableException("TEST!", null)).thenReturn(jwks);
        assertThat(provider.getJwks(), equalTo(jwks));
        verify(fallback, times(2)).getJwks();
    }

    @Test
    public void shouldNoteRetryMoreThanOnce() throws Exception {
        when(fallback.getJwks()).thenThrow(new SigningKeyUnavailableException("TEST!", null));
        
        expectedException.expect(SigningKeyUnavailableException.class);
        try {
            provider.getJwks();
        } finally {
            verify(fallback, times(2)).getJwks();
        }
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }
}
