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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RateLimitedJwksProviderTest {

    private RateLimitedJwksProvider provider;

    @Mock
    private JwksProvider fallback;

    @Mock
    private Jwk jwk;

    @Mock
    private Bucket bucket;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private List<Jwk> jwks;

    @Before
    public void setUp() throws Exception {
        provider = new RateLimitedJwksProvider(fallback, bucket);
        
        jwks = Arrays.asList(jwk);
    }

    @Test
    public void shouldFailToGetWhenBucketIsEmpty() throws Exception {
        when(bucket.consume()).thenReturn(false);
        expectedException.expect(RateLimitReachedException.class);
        provider.getJwks();
    }

    @Test
    public void shouldGetWhenBucketHasTokensAvailable() throws Exception {
        when(bucket.consume()).thenReturn(true);
        when(fallback.getJwks()).thenReturn(jwks);
        assertThat(provider.getJwks(), equalTo(jwks));
        verify(fallback).getJwks();
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), equalTo(fallback));
    }

}
