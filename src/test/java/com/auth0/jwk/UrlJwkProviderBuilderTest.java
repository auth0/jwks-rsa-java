package com.auth0.jwk;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;

import static com.auth0.jwk.UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class UrlJwkProviderBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String domain = "samples.auth0.com";
    private String normalizedDomain = "https://" + domain;

    @Test
    public void shouldCreateForUrl() throws Exception {
        URL urlToJwks = new URL(normalizedDomain + WELL_KNOWN_JWKS_PATH);
        assertThat(UrlJwkProviderBuilder.from(urlToJwks).build(), notNullValue());
    }

    @Test
    public void shouldCreateForDomain() {
        assertThat(UrlJwkProviderBuilder.from(domain).build(), notNullValue());
    }

    @Test
    public void shouldCreateForNormalizedDomain() {
        assertThat(UrlJwkProviderBuilder.from(normalizedDomain).build(), notNullValue());
    }

    @Test
    public void shouldFailWhenNoUrlIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without url to jwks");
        UrlJwkProviderBuilder.from((URL) null).build();
    }

    @Test
    public void shouldFailWhenNoDomainIsProvided() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot build provider without domain");
        UrlJwkProviderBuilder.from((String) null).build();
    }


    @Test
    public void shouldSupportUrlToJwksDomainWithSubPath() throws Exception {
        String urlToJwksWithSubPath = normalizedDomain + "/sub/path" + WELL_KNOWN_JWKS_PATH;
        URL url = new URL(urlToJwksWithSubPath);
        JwkProvider provider = UrlJwkProviderBuilder.from(url)
                .rateLimited(false)
                .cached(false)
                .build();
        assertThat(provider, notNullValue());
        assertThat(provider, instanceOf(UrlJwkProvider.class));
        UrlJwkProvider urlJwkProvider = (UrlJwkProvider) provider;
        assertThat(urlJwkProvider.url.toString(), equalTo(urlToJwksWithSubPath));
    }

}
