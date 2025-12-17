package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitedJwkProviderTest {

    private static final String KID = "KID";
    private RateLimitedJwkProvider provider;

    @Mock
    private JwkProvider fallback;

    @Mock
    private Jwk jwk;

    @Mock
    private Bucket bucket;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        provider = new RateLimitedJwkProvider(fallback, bucket);
    }

    @Test
    public void shouldFailToGetWhenBucketIsEmpty() throws Exception {
        when(bucket.consume()).thenReturn(false);
        expectedException.expect(RateLimitReachedException.class);
        provider.get(KID);
    }

    @Test
    public void shouldGetWhenBucketHasTokensAvailable() throws Exception {
        when(bucket.consume()).thenReturn(true);
        when(fallback.get(eq(KID))).thenReturn(jwk);
        assertThat(provider.get(KID), equalTo(jwk));
        verify(fallback).get(eq(KID));
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }

}