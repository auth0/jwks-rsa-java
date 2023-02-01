package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GuavaCachedJwkProviderTest {

    private static final String KID = "KID";
    private GuavaCachedJwkProvider provider;

    @Mock
    private JwkProvider fallback;

    @Mock
    private Jwk jwk;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        provider = new GuavaCachedJwkProvider(fallback);
    }

    @Test
    public void shouldFailToGetSingleWhenNotExists() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        when(fallback.get(anyString())).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.get(KID);
    }

    @Test
    public void shouldThrowSigningKeyNotFoundException() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        when(fallback.get(anyString())).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.get(KID);
    }

    @Test
    public void shouldThrowRateLimitReachedException() throws Exception {
        expectedException.expect(RateLimitReachedException.class);
        when(fallback.get(anyString())).thenThrow(new RateLimitReachedException(1234));
        provider.get(KID);
    }

    @Test
    public void shouldThrowNetworkException() throws Exception {
        expectedException.expect(NetworkException.class);
        when(fallback.get(anyString())).thenThrow(new NetworkException("TEST!", null));
        provider.get(KID);
    }

    @Test
    public void shouldUseFallbackWhenNotCached() throws Exception {
        when(fallback.get(eq(KID))).thenReturn(jwk);
        assertThat(provider.get(KID), equalTo(jwk));
        verify(fallback).get(eq(KID));
    }

    @Test
    public void shouldUseCachedValue() throws Exception {
        when(fallback.get(eq(KID))).thenReturn(jwk).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.get(KID);
        assertThat(provider.get(KID), equalTo(jwk));
        verify(fallback, only()).get(KID);
    }

    @Test
    public void shouldCacheWhenIdMatchesDefaultMissingIdKey() throws Exception {
        when(fallback.get(eq(GuavaCachedJwkProvider.NULL_KID_KEY))).thenReturn(jwk);
        assertThat(provider.get(GuavaCachedJwkProvider.NULL_KID_KEY), equalTo(jwk));
        verify(fallback).get(eq(GuavaCachedJwkProvider.NULL_KID_KEY));

        verifyNoMoreInteractions(fallback);
        assertThat(provider.get(GuavaCachedJwkProvider.NULL_KID_KEY), equalTo(jwk));
    }

    @Test
    public void shouldCacheWhenNullId() throws Exception {
        when(fallback.get(eq(null))).thenReturn(jwk);
        assertThat(provider.get(null), equalTo(jwk));
        verify(fallback).get(eq(null));

        verifyNoMoreInteractions(fallback);
        assertThat(provider.get(null), equalTo(jwk));
    }

    @Test
    public void shouldGetBaseProvider() {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }
}