package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.auth0.jwk.UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JwkProviderBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String domain = "samples.auth0.com";
    private String normalizedDomain = "https://" + domain;

    private JwkProvider customProvider = keyId -> null;

    @Test
    public void shouldCreateForProvider() {
        assertThat(JwkProviderBuilder.from(customProvider).build(), notNullValue());
    }

    @Test
    public void shouldFailWhenNoCustomProviderIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without custom provider");
        JwkProviderBuilder.from(null).build();
    }

    @Test
    public void shouldCreateCachedProvider() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .rateLimited(false)
                .cached(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateCachedProviderWithCustomValues() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .rateLimited(false)
                .cached(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateRateLimitedProvider() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .cached(false)
                .rateLimited(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateRateLimitedProviderWithCustomValues() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .cached(false)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProvider() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .cached(true)
                .rateLimited(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderWithCustomValues() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), equalTo(customProvider));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderByDefault() {
        JwkProvider provider = JwkProviderBuilder.from(customProvider).build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), equalTo(customProvider));
    }

    @Deprecated
    @Test
    public void shouldCreateForUrl() throws Exception {
        URL urlToJwks = new URL(normalizedDomain + WELL_KNOWN_JWKS_PATH);
        assertThat(new JwkProviderBuilder(urlToJwks).build(), notNullValue());
    }

    @Deprecated
    @Test
    public void shouldCreateForDomain() {
        assertThat(new JwkProviderBuilder(domain).build(), notNullValue());
    }

    @Deprecated
    @Test
    public void shouldCreateForNormalizedDomain() {
        assertThat(new JwkProviderBuilder(normalizedDomain).build(), notNullValue());
    }

    @Deprecated
    @Test
    public void shouldFailWhenNoUrlIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without url to jwks");
        new JwkProviderBuilder((URL) null).build();
    }

    @Deprecated
    @Test
    public void shouldFailWhenNoDomainIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without domain");
        new JwkProviderBuilder((String) null).build();
    }

    @Deprecated
    @Test
    public void shouldSupportUrlToJwksDomainWithSubPath() throws Exception {
        String urlToJwksWithSubPath = normalizedDomain + "/sub/path" + WELL_KNOWN_JWKS_PATH;
        URL url = new URL(urlToJwksWithSubPath);
        JwkProvider provider = new JwkProviderBuilder(url)
                .rateLimited(false)
                .cached(false)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(UrlJwkProvider.class));
        UrlJwkProvider urlJwkProvider = (UrlJwkProvider) provider;
        assertThat(urlJwkProvider.url.toString(), equalTo(urlToJwksWithSubPath));
    }

}