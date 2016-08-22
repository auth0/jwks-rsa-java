package com.auth0.jwk;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.SigningKeyNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
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
    public void setUp() throws Exception {
        provider = new GuavaCachedJwkProvider(fallback);
    }

    @Test
    public void shouldFailToGetSingle() throws Exception {
        expectedException.expect(SigningKeyNotFoundException.class);
        when(fallback.get(anyString())).thenThrow(new SigningKeyNotFoundException("TEST!", null));
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
}