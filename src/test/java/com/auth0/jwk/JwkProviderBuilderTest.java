package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;

import static com.auth0.jwk.UrlJwksProvider.WELL_KNOWN_JWKS_PATH;
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
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("A non-null url is required");
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
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(2));
        
        assertThat(jwksProviders.get(0), instanceOf(CachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(UrlJwksProvider.class));
    }

    @Test
    public void shouldCreateCachedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(24, TimeUnit.HOURS)
                .build();
    
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(2));

        CachedJwksProvider cachedJwksProvider = (CachedJwksProvider) jwksProviders.get(0);
        
        assertThat(cachedJwksProvider.getTimeToLive(), is(TimeUnit.HOURS.toMillis(24)));
    }

    @Test
    public void shouldCreateRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(true)
                .build();

        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));

        RateLimitedJwksProvider rateLimitedJwksProvider = (RateLimitedJwksProvider) jwksProviders.get(1);
        
        BucketImpl bucketImpl = (BucketImpl) rateLimitedJwksProvider.getBucket();
        assertThat(bucketImpl.getSize(), is(10L));
        assertThat(bucketImpl.getRateUnit(), is(TimeUnit.MINUTES));
        assertThat(bucketImpl.getRate(), is(1L));
    }

    @Test
    public void shouldCreateRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(100, 24, TimeUnit.HOURS)
                .build();

        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));

        RateLimitedJwksProvider rateLimitedJwksProvider = (RateLimitedJwksProvider) jwksProviders.get(1);
        
        BucketImpl bucketImpl = (BucketImpl) rateLimitedJwksProvider.getBucket();
        assertThat(bucketImpl.getSize(), is(100L));
        assertThat(bucketImpl.getRateUnit(), is(TimeUnit.HOURS));
        assertThat(bucketImpl.getRate(), is(24L));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(true)
                .rateLimited(true)
                .build();

        assertThat(provider, notNullValue());
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));
        
        assertThat(jwksProviders.get(0), instanceOf(CachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(RateLimitedJwksProvider.class));
        assertThat(jwksProviders.get(2), instanceOf(UrlJwksProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(24, TimeUnit.HOURS)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
    
        assertThat(provider, notNullValue());
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));
        
        assertThat(jwksProviders.get(0), instanceOf(CachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(RateLimitedJwksProvider.class));
        assertThat(jwksProviders.get(2), instanceOf(UrlJwksProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderByDefault() {
        JwkProvider provider = new JwkProviderBuilder(domain).build();
        assertThat(provider, notNullValue());
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));
        
        assertThat(jwksProviders.get(0), instanceOf(CachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(RateLimitedJwksProvider.class));
        assertThat(jwksProviders.get(2), instanceOf(UrlJwksProvider.class));
    }

    @Test
    public void shouldSupportUrlToJwksDomainWithSubPath() throws Exception {
        String urlToJwksWithSubPath = normalizedDomain + "/sub/path" + WELL_KNOWN_JWKS_PATH;
        URL url = new URL(urlToJwksWithSubPath);
        DefaultJwkProvider provider = (DefaultJwkProvider) new JwkProviderBuilder(url)
                .rateLimited(false)
                .cached(false)
                .build();

        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(1));

        UrlJwksProvider urlJwkProvider = (UrlJwksProvider) jwksProviders.get(0);
        assertThat(urlJwkProvider.url.toString(), equalTo(urlToJwksWithSubPath));
    }
    
    private List<JwksProvider> jwksProviders(JwkProvider jwkProvider) {
        JwksProvider jwksProvider;
        if(jwkProvider instanceof DefaultJwkProvider) {
            DefaultJwkProvider base = (DefaultJwkProvider)jwkProvider;
            jwksProvider = (JwksProvider) base.getBaseProvider();
        } else {
            jwksProvider = (JwksProvider) jwkProvider;
        }
        
        List<JwksProvider> list = new ArrayList<>();
        
        list.add(jwksProvider);
        
        while(jwksProvider instanceof BaseJwksProvider) {
            BaseJwksProvider baseJwksProvider = (BaseJwksProvider)jwksProvider;
            
            jwksProvider = baseJwksProvider.getBaseProvider();
        
            list.add(jwksProvider);
        }
    
        return list;
    }

    @Test
    public void shouldCreateRetryingProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(false)
                .retrying(true)
                .build();
        assertThat(provider, notNullValue());
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(2));
        
        assertThat(jwksProviders.get(0), instanceOf(RetryingJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(UrlJwksProvider.class));
    }

    @Test
    public void shouldCreateShadowCachedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(false)
                .shadowed(true)
                .build();
        assertThat(provider, notNullValue());
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(2));
        
        assertThat(jwksProviders.get(0), instanceOf(ShadowCachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(UrlJwksProvider.class));
    }
    
    @Test
    public void shouldCreateShadowCachedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(false)
                .shadowed(24, TimeUnit.HOURS)
                .build();
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(2));

        ShadowCachedJwksProvider cachedJwksProvider = (ShadowCachedJwksProvider) jwksProviders.get(0);
        
        assertThat(cachedJwksProvider.getTimeToLive(), is(TimeUnit.HOURS.toMillis(24)));
    }
    
    @Test
    public void shouldCreateCachedAndRateLimitedAndShadowedAndRetryingProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(true)
                .rateLimited(true)
                .retrying(true)
                .shadowed(true)
                .build();

        assertThat(provider, notNullValue());

        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(5));

        assertThat(jwksProviders.get(0), instanceOf(CachedJwksProvider.class));
        assertThat(jwksProviders.get(1), instanceOf(RateLimitedJwksProvider.class));
        assertThat(jwksProviders.get(2), instanceOf(ShadowCachedJwksProvider.class));
        assertThat(jwksProviders.get(3), instanceOf(RetryingJwksProvider.class));
        assertThat(jwksProviders.get(4), instanceOf(UrlJwksProvider.class));
    }
    
    @Test
    public void shouldCreateWithCustomJwksProvider() {
        JwksProvider customJwksProvider = mock(JwksProvider.class);
        
        JwkProvider provider = new JwkProviderBuilder(customJwksProvider).build();
        
        List<JwksProvider> jwksProviders = jwksProviders(provider);
        assertThat(jwksProviders, hasSize(3));

        assertThat(jwksProviders.get(jwksProviders.size() - 1), sameInstance(customJwksProvider));
    }
    
}
