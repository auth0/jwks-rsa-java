package com.auth0.jwk;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URL;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
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

    @Test
    public void shouldConsumeSingleTokenWhenRefreshOnMissRefetches() throws Exception {
        // A refresh-on-miss re-fetch happens inside a single UrlJwkProvider.get() call, which is
        // inside a single RateLimitedJwkProvider.get() call. Even though resolving a rotated kid
        // triggers two remote fetches (initial + refresh), only one rate-limit token is consumed.
        URL url = new URL("https://samples.auth0.com/.well-known/jwks.json");
        final AtomicInteger fetchCount = new AtomicInteger(0);
        JwksHttpClient rotatingClient = new JwksHttpClient() {
            @Override
            public JwksHttpResponse fetch(URL url) {
                String body = fetchCount.getAndIncrement() == 0
                        ? "{\"keys\":[{\"kid\":\"kid-A\",\"kty\":\"RSA\"}]}"
                        : "{\"keys\":[{\"kid\":\"kid-A\",\"kty\":\"RSA\"},{\"kid\":\"kid-B\",\"kty\":\"RSA\"}]}";
                return new JwksHttpResponse(body, Collections.<String, java.util.List<String>>emptyMap());
            }
        };
        RateLimitedJwkProvider rateLimited =
                new RateLimitedJwkProvider(new UrlJwkProvider(url, rotatingClient), bucket);
        when(bucket.consume()).thenReturn(true);

        Jwk keyB = rateLimited.get("kid-B");

        assertThat(keyB, notNullValue());
        assertThat(keyB.getId(), equalTo("kid-B"));
        // Two remote fetches (initial + refresh-on-miss) but exactly one token consumed.
        assertThat(fetchCount.get(), equalTo(2));
        verify(bucket, times(1)).consume();
    }

}