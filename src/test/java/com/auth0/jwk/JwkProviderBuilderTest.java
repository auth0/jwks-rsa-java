package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.auth0.jwk.UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JwkProviderBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String domain = "samples.auth0.com";
    private String normalizedDomain = "https://" + domain;

    @Test
    public void shouldCreateForUrl() throws Exception {
        URL urlToJwks = new URL(normalizedDomain + WELL_KNOWN_JWKS_PATH);
        assertThat(new JwkProviderBuilder(urlToJwks).build(), notNullValue());
    }

    @Test
    public void shouldCreateForDomain() {
        assertThat(new JwkProviderBuilder(domain).build(), notNullValue());
    }

    @Test
    public void shouldCreateForNormalizedDomain() {
        assertThat(new JwkProviderBuilder(normalizedDomain).build(), notNullValue());
    }

    @Test
    public void shouldFailWhenNoUrlIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without url to jwks");
        new JwkProviderBuilder((URL) null).build();
    }

    @Test
    public void shouldFailWhenNoDomainIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without domain");
        new JwkProviderBuilder((String) null).build();
    }

    @Test
    public void shouldCreateCachedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(false)
                .rateLimited(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(false)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(true)
                .rateLimited(true)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderByDefault() {
        JwkProvider provider = new JwkProviderBuilder(domain).build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        assertThat(baseProvider, instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), instanceOf(UrlJwkProvider.class));
    }

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

    @Test
    public void shouldCreateForUrlAndProxy() throws Exception {
        URL url = new URL(normalizedDomain + WELL_KNOWN_JWKS_PATH);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.hostname", 8080));
        JwkProvider provider = new JwkProviderBuilder(url)
                .proxied(proxy)
                .rateLimited(false)
                .cached(false)
                .build();
        assertThat(provider, notNullValue());
        UrlJwkProvider urlJwkProvider = (UrlJwkProvider) provider;
        assertThat(urlJwkProvider.proxy, equalTo(proxy));
    }
}